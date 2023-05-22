# Self-Solving Maze
The program that I created is a maze that can either be self-solved using depth-first search or breadth-first search. The program also has an option to let the user take manual control and try to solve the maze themselves. At first, I had a lot of trouble figuring out how to even start coding a self-solving maze. Although, I realized that I could see the maze as a set of nodes and edges. When trying to solve the maze manually, the program is also able to keep track of your moves and how many you are making. As you move, the path that you are taking is displayed. Once you reach the end, you are shown how many incorrect moves you made, if any. Since DFS and BFS aren't the most efficient searching algorithms, the program used Dijkstra's algorithm instead. Dijkstra's algorithm is used to find the shortest path between nodes in a graph, and since I was treating my maze as a collection of nodes and graphs, it was perfect for my use case.
