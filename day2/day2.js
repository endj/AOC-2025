const fs = require("fs");

const data = fs.readFileSync("input.txt", "utf8");

const part1 = () => {
  return data
    .split("\n")
    .filter(Boolean)
    .reduce((acc, lines) => acc + validate(lines.split(" ")), 0);
};

const part2 = () => {
  return data
    .split("\n")
    .filter(Boolean)
    .reduce(
      (acc, lines) => acc + validate(lines.split(" "), (check = true)),
      0,
    );
};

function removeElementAtIndex(array, index) {
  return array.slice(0, index).concat(array.slice(index + 1));
}

const validate = (numbers, check = false) => {
  let increasing = numbers[0] - numbers[1] < 0;
  for (let i = 0; i < numbers.length - 1; i++) {
    const increase = numbers[i] - numbers[i + 1] < 0;
    const diff = Math.abs(numbers[i] - numbers[i + 1]);

    if (increase != increasing || ![1, 2, 3].find((i) => i === diff)) {
      if (!check) return 0;
      for (let j = 0; j < numbers.length; j++) {
        if (validate(removeElementAtIndex(numbers, j)) == 1) {
          return 1;
        }
      }
      return 0;
    }
    increasing = increase;
  }
  return 1;
};

const one = part1();
const two = part2();
console.log(`Part one: ${one}, Part two: ${two}`);
