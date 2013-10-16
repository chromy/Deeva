from jnius import autoclass

#MyTrial = autoclass('MyTrial')
#unicorn = MyTrial()

source = []
fileName = 'MyTrial.java'

def load():
    file = open(fileName, 'r')
    for line in file:
        source.append(line)
    file.close()

def getSource():
    load()
    return source 
