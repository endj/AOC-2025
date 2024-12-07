import sys


name = "input"
if len(sys.argv) == 2:
    name = sys.argv[1]

def solve(target, numbers):
    def rec(sum, index):
        if sum > target: return 0
        if index == len(numbers): # all numbers used
            return target if sum == target else 0

        next = numbers[index]

        if rec(sum+next, index + 1): return target
        if rec(sum*next, index + 1): return target
        if rec(int(f'{sum}{next}'), index + 1): return target

        return 0

    return rec(numbers[0], 1)

with open(name) as f:
    sum = 0
    for problem in f.readlines():
        parts = problem.split(" ")
        target = int(parts[0].replace(":", "").strip())
        numbers = list(map(int, parts[1:]))
        ans = solve(target, numbers) # Yolo bruteforce
        sum += ans

    print(sum)
