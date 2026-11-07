def a = assembly { place(importStep(args[0])) }

DisplayOption o = DisplayOption.builder()
        .edges(true, Color.BLACK)
        .perspective(30)
        .light(1, new Vec(200, -200, 300), Color.WHITE)
        .light(2, new Vec(-200, 100, 100), Color.ORANGE)
        .transparentBackground()
        .build()

if (args[1])
    a.display(o, args[1])
if (args.length > 2 && args[2])
    a.display(o, args[2])
