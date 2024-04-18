import requests
import time

_wait = 0.5

def get_freq(term):
    response = None
    while True:
        try:
            response = requests.get('https://api.datamuse.com/words?sp='+term+'&md=f&max=1').json()
        except:
            print ("Could not get response. Sleep and retry...")
            time.sleep(_wait)
            continue
        break
    freq = 0.0 if len(response)==0 else float(response[0]['tags'][0][2:])
    return freq

def get_freqs(terms):
    nums = []

    for term in terms:
        nums.append((term, get_freq(term)))

    return nums

def file_read(name):
    lines = []

    with open(name, "r") as file:
        for line in file:
            lines.append(line.strip())

    return lines

def file_write(terms):
    with open("frequency.txt", "w") as file:
        for term in terms:
            (word, frequency) = term
            file.write(word + " " + str(frequency) + "\n")
    
file_write (get_freqs(file_read("resources/words.txt")))