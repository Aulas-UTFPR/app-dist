# rabbit_pika.py
from __future__ import annotations
import json, threading, time, traceback
from typing import Optional, Dict, Callable
from queue import Queue, Empty
from collections import deque
import pika

class _PublishItem:
    def __init__(self, rk: str, body: bytes, props: pika.BasicProperties):
        self.rk = rk
        self.body = body
        self.props = props

class PikaClient(threading.Thread):
    def __init__(self, amqp_url: str, exchange: str, on_user_msg: Callable[[str, bytes], None]):
        super().__init__(daemon=True)
        self.amqp_url = amqp_url
        self.exchange = exchange
        self.on_user_msg = on_user_msg  # callback(username, body)
        self._conn: Optional[pika.SelectConnection] = None
        self._ch: Optional[pika.channel.Channel] = None
        self._stop_evt = threading.Event()
        self._pub_q: Queue[_PublishItem] = Queue()
        self._consumers: Dict[str, tuple[str, str]] = {}
        self._ready = False
        self._pending = deque()

    def stop(self):
        self._stop_evt.set()
        try:
            if self._conn and self._conn.is_open:
                self._conn.ioloop.stop()
        except Exception:
            pass

    def publish(self, routing_key: str, payload: dict):
        body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        props = pika.BasicProperties(content_type="application/json", delivery_mode=2)
        self._pub_q.put(_PublishItem(routing_key, body, props))

    def ensure_user_queue(self, username: str, *, ttl_ms: Optional[int] = None):
        def _do():
            qargs = {}
            if ttl_ms:
                qargs["x-message-ttl"] = ttl_ms
            self._ch.queue_declare(
                queue=f"chat.user.{username}",
                durable=True,
                auto_delete=False,
                arguments=qargs,
                callback=lambda _f: self._ch.queue_bind(
                    queue=f"chat.user.{username}",
                    exchange=self.exchange,
                    routing_key=f"user.{username}",
                    callback=lambda _f2: None
                )
            )
        self._call_in_ioloop(_do)

    def start_user_consumer(self, username: str):
        def _do():
            if username in self._consumers:
                return
            qname = f"chat.user.{username}"
            def _on_msg(ch, method, props, body: bytes):
                try:
                    self.on_user_msg(username, body)
                except Exception:
                    traceback.print_exc()
            tag = self._ch.basic_consume(queue=qname, on_message_callback=_on_msg, auto_ack=True)
            self._consumers[username] = (qname, tag)
        self._call_in_ioloop(_do)

    def start_user_consumer(self, username: str):
        def _do():
            if username in self._consumers:
                return
            qname = f"chat.user.{username}"
            def _on_msg(ch, method, props, body: bytes):
                try:
                    ok = False
                    try:
                        ok = self.on_user_msg(username, body)   # <-- bool
                    except Exception:
                        traceback.print_exc()
                    if ok:
                        ch.basic_ack(delivery_tag=method.delivery_tag)
                    else:
                        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
                except Exception:
                    traceback.print_exc()
                    try:
                        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
                    except Exception:
                        pass
            tag = self._ch.basic_consume(queue=qname, on_message_callback=_on_msg, auto_ack=False)
            self._consumers[username] = (qname, tag)
        self._call_in_ioloop(_do)

    def stop_user_consumer(self, username: str):
        def _do():
            tup = self._consumers.pop(username, None)
            if tup:
                _q, tag = tup
                try:
                    self._ch.basic_cancel(tag)
                except Exception:
                    pass
        self._call_in_ioloop(_do)

    def run(self):
        backoff = 1.0
        while not self._stop_evt.is_set():
            try:
                params = pika.URLParameters(self.amqp_url)
                params.heartbeat = 30
                params.blocked_connection_timeout = 300
                params.client_properties = {"connection_name": "fastapi-chat"}

                self._ready = False
                self._pending.clear()

                self._conn = pika.SelectConnection(
                    parameters=params,
                    on_open_callback=self._on_conn_open,
                    on_open_error_callback=self._on_conn_open_err,
                    on_close_callback=self._on_conn_closed,
                )
                print("[pika] starting ioloop...")
                self._conn.ioloop.start()
                backoff = 1.0
            except Exception:
                traceback.print_exc()
                if self._stop_evt.is_set():
                    break
                time.sleep(backoff)
                backoff = min(30.0, backoff * 2)

    def _on_conn_open(self, conn):
        print("[pika] connection open")
        conn.channel(on_open_callback=self._on_channel_open)

    def _on_conn_open_err(self, conn, err):
        print(f"[pika] open error: {err!r}")
        try: conn.ioloop.stop()
        except Exception: pass

    def _on_conn_closed(self, conn, reason):
        print(f"[pika] connection closed: {reason!r}")
        self._consumers.clear()
        try: conn.ioloop.stop()
        except Exception: pass

    def _on_channel_open(self, ch):
        self._ch = ch
        self._ch.exchange_declare(
            exchange=self.exchange,
            exchange_type="topic",
            durable=True,
            callback=lambda _f: self._on_exchange_ready()
        )

    def _on_exchange_ready(self):
        self._ready = True
        self._start_pump()
        # flush anything that arrived before channel/exchange was ready
        while self._pending:
            try:
                fn = self._pending.popleft()
                fn()
            except Exception:
                traceback.print_exc()

    def _start_pump(self):
        self._schedule_pub()

    def _schedule_pub(self):
        if self._stop_evt.is_set() or not (self._conn and self._conn.is_open):
            return
        try:
            for _ in range(200):
                item = self._pub_q.get_nowait()
                self._ch.basic_publish(
                    exchange=self.exchange,
                    routing_key=item.rk,
                    body=item.body,
                    properties=item.props
                )
        except Empty:
            pass
        except Exception:
            traceback.print_exc()
        finally:
            self._conn.ioloop.call_later(0.01, self._schedule_pub)

    def _call_in_ioloop(self, fn):
        def _runner():
            if not self._ready:
                self._pending.append(fn)
            else:
                fn()
        if self._conn and self._conn.is_open:
            self._conn.ioloop.add_callback_threadsafe(_runner)
