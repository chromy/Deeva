import os

# add this folder to classpath
classpath = os.environ.get("CLASSPATH", "")
os.environ["CLASSPATH"] = os.path.dirname('.') + os.pathsep + classpath
print os.environ["CLASSPATH"]

#path = os.environ.get("CLASSPATH", "")
#os.environ["CLASSPATH"] = os.path.dirname(os.path.abspath(__file__)) + os.pathsep + classpath
#print os.environ["CLASSPATH"]


from jnius import autoclass

System = autoclass('java.lang.System')
print System.getProperty('java.class.path')

Stack = autoclass('java.util.Stack')
stack = Stack()
stack.push('hello')
stack.push('world')
print stack.pop()
print stack.pop()

MyTrial = autoclass('MyTrial')
unicorn = MyTrial()

def printTrialNo():
    return unicorn.getTrialNo()
