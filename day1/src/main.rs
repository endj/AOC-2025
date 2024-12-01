use std::cmp::Reverse;
use std::collections::BinaryHeap;
use std::collections::HashMap;
use std::fs::File;
use std::io::{self, BufRead};

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let one = part1()?;
    let two = part2()?;
    println!("Part one: {}\nPart Two: {}", one, two);
    Ok(())
}

fn part1() -> Result<i32, Box<dyn std::error::Error>> {
    let mut left_heap = BinaryHeap::new();
    let mut right_heap = BinaryHeap::new();
    let file = File::open("input.txt")?;
    let reader = io::BufReader::new(file);

    for line in reader.lines() {
        let line = line?;
        if line.is_empty() {
            continue;
        }
        let mut parts = line.split_whitespace();
        let left: i32 = parts.next().ok_or("left")?.parse()?;
        let right: i32 = parts.next().ok_or("right")?.parse()?;
        left_heap.push(Reverse(left));
        right_heap.push(Reverse(right));
    }

    let mut sum = 0;
    while let (Some(Reverse(left)), Some(Reverse(right))) = (left_heap.pop(), right_heap.pop()) {
        sum += (left - right).abs();
    }
    Ok(sum)
}

fn part2() -> Result<i32, Box<dyn std::error::Error>> {
    let file = File::open("input.txt")?;
    let reader = io::BufReader::new(file);
    let mut count_map = HashMap::new();
    let mut left_values = Vec::new();

    for line in reader.lines() {
        let line = line?;
        if line.is_empty() {
            continue;
        }
        let mut parts = line.split_whitespace();
        let left: i32 = parts.next().ok_or("left")?.parse()?;
        let right: i32 = parts.next().ok_or("right")?.parse()?;

        left_values.push(left);
        let counter = count_map.entry(right).or_insert(0);
        *counter += 1;
    }
    let mut sum = 0;
    for left in left_values {
        if let Some(&count) = count_map.get(&left) {
            sum += left * count;
        }
    }
    Ok(sum)
}
