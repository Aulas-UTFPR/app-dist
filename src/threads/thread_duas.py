import threading
import time

def print_numbers():
    for i in range(5):
        print(i)
        time.sleep(0.1)

def print_letters():
    for letter in ['A', 'B', 'C', 'D', 'E']:
        print(letter)
        time.sleep(0.1)

thread1 = threading.Thread(target=print_numbers)
thread2 = threading.Thread(target=print_letters)
thread1.start()
thread2.start()
thread1.join() 
thread2.join()