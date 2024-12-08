use std::collections::HashMap;
use std::fs::File;
use std::io::{self, BufRead};
use std::time::Instant;

fn load_input() -> Vec<Vec<u8>> {
    let file = File::open("input").expect("input file not found");
    let reader = io::BufReader::new(file);
    return reader
        .lines()
        .filter_map(|line| line.ok())
        .map(|line| line.into_bytes())
        .collect();
}

fn main() -> io::Result<()> {
    let start = Instant::now();

    let grid: Vec<Vec<u8>> = load_input();
    let mut locations: HashMap<u8, Vec<usize>> = HashMap::new();
    let rows = grid.len();
    let cols = grid[0].len();
    let mut pt1 = vec![vec![0u8; cols]; rows];
    let mut pt2 = vec![vec![0u8; cols]; rows];

    for (y, row) in grid.iter().enumerate() {
        for (x, &value) in row.iter().enumerate() {
            if value != b'.' {
                locations
                    .entry(value)
                    .or_insert_with(Vec::new)
                    .push((y << 16) | x);
            }
        }
    }

    for (_, entry) in &locations {
        for &a in entry {
            let ay = a >> 16;
            let ax = a & 0xFFFF;

            for &b in entry {
                let by = b >> 16;
                let bx = b & 0xFFFF;

                if ax == bx && ay == by {
                    continue;
                }
                let dy = ay as isize - by as isize;
                let dx = ax as isize - bx as isize;

                let loc_ay = ay as isize - (dy * 2);
                let loc_ax = ax as isize - (dx * 2);
                if loc_ay >= 0 && loc_ay < rows as isize && loc_ax >= 0 && loc_ax < cols as isize {
                    pt1[loc_ay as usize][loc_ax as usize] = 1;
                }

                let loc_by = by as isize + (dy * 2);
                let loc_bx = bx as isize + (dx * 2);
                if loc_by >= 0 && loc_by < rows as isize && loc_bx >= 0 && loc_bx < cols as isize {
                    pt1[loc_by as usize][loc_bx as usize] = 1;
                }

                mark_lines(&mut pt2, ay, ax, dy, dx);
                mark_lines(&mut pt2, by, bx, dy, dx);
            }
        }
    }

    let mut sum1 = 0;
    for row in &pt1 {
        for &v in row {
            sum1 += v as usize;
        }
    }

    let mut sum2 = 0;
    for row in &pt2 {
        for &v in row {
            sum2 += v as usize;
        }
    }

    println!("Part one: {}", sum1);
    println!("Part two: {}", sum2);
    let duration = start.elapsed();

    // Print the elapsed time in seconds and milliseconds
    println!("Time elapsed: {:?}", duration);
    Ok(())
}

fn mark_lines(grid: &mut Vec<Vec<u8>>, start_y: usize, start_x: usize, dy: isize, dx: isize) {
    let mut y = start_y as isize + dy;
    let mut x = start_x as isize + dx;

    while y >= 0 && y < grid.len() as isize && x >= 0 && x < grid[0].len() as isize {
        grid[y as usize][x as usize] = 1;
        y += dy;
        x += dx;
    }
}
