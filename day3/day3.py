def find_next(idx, data, end):
    if not data[idx].isnumeric():
        return None, idx + 1

    comma = None
    for i in range(idx, end):
        if data[i] == ",":
            comma = i
            break
        if not data[i].isnumeric():
            return None, i + 1
    if not comma:
        return None, len(data)

    for i in range(comma + 1, len(data)):
        if data[i] == ")":
            return data[idx:i], i
        if not data[i].isnumeric():
            return None, i + 1

    return None, len(data)


def safe_ranges(data):
    res = []
    i = data.find("don't()")
    res.append((0,i))
    needle = i + len("don't()")
    if needle == -1:
        return res

    while needle < len(data):
       do = data.find("do()", needle)
       if do == -1: return res

       dont = data.find("don't()", do + len("do()"))
       if dont == -1:
           res.append((do+ len("do()"), len(data)))
           return res

       res.append((do + len("do()"), dont))
       needle = dont + len("don't()")
    return res


def mults_in_range(data, start, end):
    needle = start
    result = []

    while needle < end:
        idx = data.find("mul(", needle)
        if idx == -1:
            break
        idx += len("mul(")
        (res, i) = find_next(idx, data, end)
        if res is not None:
            result.append(res)
        if i == len(data):
            break
        needle = i
        next_mult = data.find("mul(", needle)

    return result

def sum_up_mults(mults):
    result = [i.split(",") for i in mults]
    return sum([(int(i[0]) * int(i[1])) for i in result])

with open("test.txt") as f:
    data = f.read()
    summed = 0

    for (start,end) in safe_ranges(data):
        summed += sum_up_mults(mults_in_range(data, start, end))

    print("Part one:", sum_up_mults(mults_in_range(data, 0, len(data))))
    print("Part two: ", summed)
