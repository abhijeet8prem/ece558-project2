
# reference: https://learn.adafruit.com/circuitpython-on-raspberrypi-linux/digital-i-o

# imports
import time
import board
import digitalio

# comment to the terminal to check if the script is running
print("hello blinky!")


led = digitalio.DigitalInOut(board.D18)         #
led.direction = digitalio.Direction.OUTPUT

button = digitalio.DigitalInOut(board.D4)
button.direction = digitalio.Direction.INPUT
button.pull = digitalio.Pull.UP

while True:
    led.value = not button.value # light up led when button is pressed!