from jnius import autoclass

MyTrial = autoclass('MyTrial')
unicorn = MyTrial()

def printTrialNo():
    return unicorn.getTrialNo()
