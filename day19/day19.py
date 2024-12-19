

with open("input") as f:
    patterns, towels = f.read().split("\n\n")
    patterns = [p.strip() for p in patterns.split(",")]
    towels = [t for t in towels.split("\n") if t]

    def check(towel, index, cache):
        if index in cache: return cache[index]
        if index == len(towel): return 1

        ways = 0
        for pattern in patterns:
            if towel.startswith(pattern, index):
                ways += check(towel, index + len(pattern), cache)
        cache[index] = ways
        return ways

    pt1 = 0
    pt2 = 0
    for t in towels:
        val = check(t, 0 , {})
        pt2 += val
        if val > 0: pt1 += 1
    print("Part one", pt1, "\nPart two", pt2)
