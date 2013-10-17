from jnius import autoclass

#MyTrial = autoclass('MyTrial')
#unicorn = MyTrial()

fileName = 'MyTrial.java'

def load():
    source = []
    file = open(fileName, 'r')
    for line in file:
        source.append(line)
    file.close()
    return source
    
