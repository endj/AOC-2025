package main

import (
	"fmt"
	"os"
	"strconv"
	"strings"
)

func main() {
	filename := "input"
	if len(os.Args) > 1 {
		filename = os.Args[1]
	}
	data, err := os.ReadFile(filename)
	if err != nil {
		panic("Failed to read file")
	}

	parts := strings.Split(string(data), "\n\n")
	pages, sequences := parts[0], parts[1]

	dependencies := make(map[string]map[string]bool)
	for _, page := range strings.Split(pages, "\n") {
		parts := strings.Split(page, "|")
		left, right := parts[0], parts[1]
		if _, exists := dependencies[right]; !exists {
			dependencies[right] = make(map[string]bool)
		}
		dependencies[right][left] = true
	}

	seqList := make([][]string, 0)
	for _, seq := range strings.Split(sequences, "\n") {
		seqList = append(seqList, strings.Split(seq, ","))
	}

	pt1 := 0
	invalidSequences := make([][]string, 0)
	for _, sequence := range seqList {
		valid := true
	out:
		for i := 0; i < len(sequence); i++ {
			if _, exists := dependencies[sequence[i]]; exists {
				deps := dependencies[sequence[i]]
				for j := i + 1; j < len(sequence); j++ {
					if _, depExists := deps[sequence[j]]; depExists {
						valid = false
						break out
					}
				}
			}
		}
		if valid {
			val, _ := strconv.Atoi(sequence[len(sequence)/2])
			pt1 += val
		} else {
			invalidSequences = append(invalidSequences, sequence)
		}
	}

	pt2 := 0
	for _, seq := range invalidSequences {
		sortedSeq := quickSelectMid(dependencies, seq)
		val, _ := strconv.Atoi(sortedSeq)
		pt2 += val
	}

	fmt.Println("Part One:", pt1)
	fmt.Println("Part Two:", pt2)
}

func quickSelectMid(dependencies map[string]map[string]bool, items []string) string {
	visited := make(map[string]bool)
	itemSet := make(map[string]bool)
	sorted := 0

	for _, item := range items {
		itemSet[item] = true
	}

	mid := len(items)/2 + 1
	for _, item := range items {
		if val, ok := dfs(item, dependencies, itemSet, visited, &sorted, mid); ok {
			return val
		}
	}

	panic("Unable to find mid")
}

func dfs(node string, dependencies map[string]map[string]bool, itemSet map[string]bool, visited map[string]bool, sorted *int, index int) (string, bool) {
	if visited[node] {
		return "", false
	}
	visited[node] = true

	if deps, exists := dependencies[node]; exists {
		for neighbor := range deps {
			if _, exists := itemSet[neighbor]; exists {
				if val, ok := dfs(neighbor, dependencies, itemSet, visited, sorted, index); ok {
					return val, true
				}
			}
		}
	}

	*sorted += 1
	if *sorted == index {
		return node, true
	}
	return "", false
}
