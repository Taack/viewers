
def a = assembly { place(importStep(args[0])) }

if (args[1])
    a.display(args[1])
if (args[2])
    a.display(args[2])
