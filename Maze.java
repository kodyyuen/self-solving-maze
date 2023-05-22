import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.HashMap;

/*
 * Documentation:
 * Arrow keys control manual movement and where you move in the maze
 * D displays depth-first search
 * B displays breadth-first search
 * R resets the maze and displays a new one
 * T toggles the viewing of the visited paths in the maze
 *
 * Extra credit:
 * Toggle
 * Displays number of wrong moves after the maze has been completed
 */

// represents a "node" in the maze
class Node {
  int pos; // tells where in the board the node is 
  Node top;
  Node left;
  Node right;
  Node bottom;
  Color color;

  int size = 10; // represents the size of the square 

  // constructor for Node
  Node(int pos, Node top, Node left, Node right, Node bottom, Color color) {
    this.pos = pos;
    this.top = top;
    this.left = left;
    this.right = right;
    this.bottom = bottom;
    this.color = color;
  }

  // default constructor for Node that takes only a position and sets all neighbors to null
  Node(int pos) {
    this.pos = pos;
    this.top = null;
    this.left = null;
    this.right = null;
    this.bottom = null;
    this.color = Color.WHITE;
  }

  // EFFECT: sets the left Node to the given cell
  void changeLeft(Node n) {
    this.left = n;
  }

  // EFFECT: sets the right Node to the given cell
  void changeRight(Node n) {
    this.right = n;
  }

  // EFFECT: sets the top Node to the given cell
  void changeTop(Node n) {
    this.top = n;
  }

  // EFFECT: sets the bottom Node to the given cell
  void changeBottom(Node n) {
    this.bottom = n;
  }

  // EFFECT: changes the color of the given cell to the given color
  void changeColor(Color c) {
    this.color = c;
  }

  // draws a node using its neighbors to tell where walls should be 
  WorldImage drawNode() {
    WorldImage drawnNode = 
        new RectangleImage(this.size, this.size, OutlineMode.SOLID, this.color);
    WorldImage verticalLineImage = 
        new LineImage(new Posn(0, this.size), Color.BLACK);
    WorldImage horizontalLineImage = 
        new LineImage(new Posn(this.size, 0), Color.BLACK);

    // moves pinhole for the possibility of a left overlay
    drawnNode = drawnNode.movePinhole(-1 * (double)this.size / 2, 0);
    if (this.left == null) {
      drawnNode = new OverlayImage(verticalLineImage, drawnNode);
    }

    // moves pinhole for the possibility of a right overlay
    drawnNode = drawnNode.movePinhole(this.size, 0);
    if (this.right == null) {
      drawnNode = new OverlayImage(verticalLineImage, drawnNode);
    }

    // moves pinhole for the possibility of a top overlay
    drawnNode = drawnNode.movePinhole(-1 * (double)this.size / 2, -1 * (double)this.size / 2);
    if (this.top == null) {
      drawnNode = new OverlayImage(horizontalLineImage, drawnNode);
    }

    // moves pinhole for the possibility of a bottom overlay
    drawnNode = drawnNode.movePinhole(0, this.size);
    if (this.bottom == null) {
      drawnNode = new OverlayImage(horizontalLineImage, drawnNode);
    }

    return drawnNode;
  }
}

// represents a connection between two nodes
class Edge {
  int weight; // weight represents the value of an edge
  Node node1;
  Node node2;

  // constructor for Edge
  Edge(int weight, Node node1, Node node2) {
    this.weight = weight;
    this.node1 = node1;
    this.node2 = node2;
  }
}

// comparator for sorting a list of edges by weight
class EdgeComparator implements Comparator<Edge> {
  // method used by comparator for sorting
  public int compare(Edge o1, Edge o2) {
    return o1.weight - o2.weight;
  }
}

//Represents a mutable collection of items
interface ICollection<T> {
  // Is this collection empty?
  boolean isEmpty();
  
  // EFFECT: adds the item to the collection
  void add(T item);
  
  // Returns the first item of the collection
  // EFFECT: removes that first item
  T remove();
}

// represents a deque where the items are FIFO
class Stack<T> implements ICollection<T> {
  Deque<T> contents;
  
  Stack() {
    this.contents = new ArrayDeque<T>();
  }
  
  // Is this collection empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }
  
  // Returns the first item of the collection
  // EFFECT: removes that first item
  public T remove() {
    return this.contents.removeFirst();
  }
  
  // EFFECT: adds the item to the front of the collection
  public void add(T item) {
    this.contents.addFirst(item);
  }
}

// represents a deque where the items are FILO
class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  Queue() {
    this.contents = new ArrayDeque<T>();
  }
  
  // Is this collection empty?
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }
  
  // Returns the last item of the collection
  // EFFECT: removes that first item
  public T remove() {
    return this.contents.removeFirst();
  }
  
  // EFFECT: adds the item to the end of the collection
  public void add(T item) {
    this.contents.addLast(item); 
  }
}

// represents the Maze 
class Maze extends World {
  int length; // number of columns in the Maze
  int height; // number of rows in the Maze

  // holds the status of if the maze is finished or not
  boolean finished = false;

  //list that will hold the initial edges of the Maze, creating a grid
  ArrayList<Edge> edges = new ArrayList<Edge>(); 

  // 2 dimensional list that will hold the Nodes that will be used to make the maze
  ArrayList<ArrayList<Node>> board = new ArrayList<ArrayList<Node>>();

  // will be used to hold the edges that are given by the Kruskal's algorithm portion of code
  ArrayList<Edge> edgesInTheMaze = new ArrayList<Edge>();

  // instantiates a new random
  Random rand = new Random();

  // will be used for the manual solution steps
  Node currentNode;

  // keeps track of the nodes that were visited to be used to animate the maze
  ArrayList<Node> visitedNodes = new ArrayList<Node>();

  // holds nodes to be able to create an animation effect
  ArrayList<Node> animatedNodes = new ArrayList<Node>();

  // will be used for the search algorithms 
  HashMap<Node, Node> cameFromNode = new HashMap<Node, Node>(); 

  // holds the status of the maze's toggle mode
  boolean toggle = false;

  // holds the nodes that will be toggled
  ArrayList<Node> toggleList = new ArrayList<Node>();

  // keeps count of how many wrong moves have been done
  int wrongMoves = 0;

  // keeps track of if the maze has been solved or not
  boolean solved = false;

  // constructor for Maze
  Maze(int length, int height) {
    this.length = length;
    this.height = height;
    this.createMaze();
  }

  // constructor for the Maze that includes a random value, for testing
  Maze(int length, int height, Random rand) {
    this.length = length;
    this.height = height;
    this.rand = rand;
    this.createMaze();
  }

  // EFFECT: used to set up the board, edges, and edgesInTheMaze lists to create the Maze
  void createMaze() {
    // sets up the nodes of the board, with each neighbor being null to start
    ArrayList<Node> temp = new ArrayList<Node>();
    for (int y = 0; y < this.height; y++) {
      for (int x = 0; x < this.length; x++) {
        temp.add(new Node(y * this.length + x));
      }
      this.board.add(temp);
      temp = new ArrayList<Node>();
    }

    // *NOTE* when referring to the edges here, we think about the edges as the walls that separate
    // the nodes, creating the 'grid' that we will use to create the actual maze later on
    // sets the initial horizontal edges of the Maze
    for (int y = 0; y < this.height; y++) {
      for (int x = 1; x <= this.length - 1; x++) {
        edges.add(new Edge(rand.nextInt(1000), 
            this.board.get(y).get(x - 1), this.board.get(y).get(x)));
      }
    }

    // sets the initial vertical edges of the Maze
    for (int y = 1; y < this.height; y++) {
      for (int x = 0; x < this.length; x++) {
        edges.add(new Edge(rand.nextInt(1000), 
            this.board.get(y - 1).get(x), this.board.get(y).get(x)));
      }
    }

    //sorts the list of edges to be from least to greatest in weight
    Collections.sort(this.edges, new EdgeComparator());

    // set the edgesInTheMaze list to be equal to the output of the edgesInMaze method, which 
    // uses Kruskal's algorithm to produce the edgesInTheMaze
    this.edgesInTheMaze = this.edgesInMaze();


    // setting neighbors: go through the edgesInTheMaze now and use those edges to set the 
    // neighbors of the appropriate cells, preparing them for drawing
    for (int i = 0; i < this.edgesInTheMaze.size(); i++) {
      Node temp1 = this.edgesInTheMaze.get(i).node1;
      Node temp2 = this.edgesInTheMaze.get(i).node2;

      // if temp1 is to the left of temp2
      if (temp1.pos - temp2.pos == -1) {
        temp1.changeRight(temp2);
        temp2.changeLeft(temp1);
      }

      // if temp1 is to the right of temp2
      if (temp1.pos - temp2.pos == 1) {
        temp1.changeLeft(temp2);
        temp2.changeRight(temp1);
      }

      // if temp1 is above temp2
      if (temp1.pos - temp2.pos == this.length) {
        temp1.changeTop(temp2);
        temp2.changeBottom(temp1);
      }

      // if temp1 is below temp2 
      if (temp1.pos - temp2.pos == -1 * this.length) {
        temp2.changeTop(temp1);
        temp1.changeBottom(temp2);
      }
    }

    // setting up the list for the DFS BFS and manual entry solution modes

    this.currentNode = this.board.get(0).get(0);
  }

  // method that creates the list of edges in the maze which is used in createMaze,
  // the method uses Kruskal's algorithm to create this list
  ArrayList<Edge> edgesInMaze() {
    // KRUSKAL'S ALGO , PSEDUO CODE 
    // -------------------------------------
    // initialize every node's representative to itself
    // While(there's more than one tree)
    //   Pick the next cheapest edge of the graph: suppose it connects X and Y.
    //   If find(representatives, X) equals find(representatives, Y):
    //     discard this edge // they're already connected
    //   Else:
    //     Record this edge in edgesInTree
    //     union(representatives,
    //     find(representatives, X),
    //     find(representatives, Y))
    // Return the edgesInTree

    HashMap<Integer, Integer> representatives = new HashMap<Integer, Integer>();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();

    // initialize all of the positions in the board to have their values be themselves
    for (int i = 0; i < this.length * this.height; i++) {
      representatives.put(i, i);
    }

    // goes through the edges list to create the edgesInTree list
    while (edgesInTree.size() < this.length * this.height - 1) {
      Edge temp = this.edges.remove(0);
      int highestRep1 = find(representatives, representatives.get(temp.node1.pos));
      int highestRep2 = find(representatives, representatives.get(temp.node2.pos));
      if (highestRep1 != highestRep2) {
        edgesInTree.add(temp);
        representatives.put(highestRep1, highestRep2);
      }
      // else, we're discarding the edge
    }
    return edgesInTree;
  }

  // Method to get the highest representative of a key
  int find(HashMap<Integer, Integer> rep, int search) {
    int upperRepresentative = rep.get(search);
    if (upperRepresentative != search) {
      return find(rep, upperRepresentative);
    }
    return upperRepresentative;
  }

  // EFFECT: runs the search algorithm
  void searchHelp(Node from, Node to, ICollection<Node> workList) {
    workList.add(from);
    Deque<Node> alreadySeen = new ArrayDeque<Node>();

    while (!workList.isEmpty()) {
      Node next = workList.remove();
      this.visitedNodes.add(next);

      if (next.equals(to)) {
        this.finished = true;
        break;
      }

      else if (alreadySeen.contains(next)) {
        // don't care, discard
      }
      else {
        if (next.top != null && !alreadySeen.contains(next.top)) {
          workList.add(next.top);
          this.cameFromNode.put(next.top, next);
        }
        if (next.bottom != null && !alreadySeen.contains(next.bottom)) {
          workList.add(next.bottom);
          this.cameFromNode.put(next.bottom, next);
        }
        if (next.left != null && !alreadySeen.contains(next.left)) {
          workList.add(next.left);
          this.cameFromNode.put(next.left, next);
        }
        if (next.right != null && !alreadySeen.contains(next.right)) {
          workList.add(next.right);
          this.cameFromNode.put(next.right, next);
        }
        alreadySeen.add(next);
      }
    } 
  }

  // EFFECT: creates a direct path between the beginning and the end of the maze
  // by mutating cells' colors
  void directPath() {
    Node next = this.board.get(this.height - 1).get(this.length - 1);
    while (!(next.equals(this.board.get(0).get(0)))) {
      next.changeColor(Color.YELLOW);
      next = cameFromNode.get(next);
    }
    this.finished = false;
    this.solved = true;
  }

  // EFFECT: for allowing the user to use their mouse keys to go through the maze.
  // changes the color of the cells as the current node moves through
  void manualEntry(String move) {
    if (move.equals("up") && this.currentNode.top != null) {
      this.currentNode.changeColor(Color.BLUE);
      if (!this.cameFromNode.containsKey(this.currentNode.top)) {
        this.cameFromNode.put(this.currentNode.top, this.currentNode);
      }
      this.currentNode = this.currentNode.top;
      this.currentNode.changeColor(Color.ORANGE);
    }

    if (move.equals("down") && this.currentNode.bottom != null) {
      this.currentNode.changeColor(Color.BLUE);
      if (!this.cameFromNode.containsKey(this.currentNode.bottom)) {
        this.cameFromNode.put(this.currentNode.bottom, this.currentNode);
      }
      this.currentNode = this.currentNode.bottom;
      this.currentNode.changeColor(Color.ORANGE);
    }

    if (move.equals("left") && this.currentNode.left != null) {
      this.currentNode.changeColor(Color.BLUE);
      if (!this.cameFromNode.containsKey(this.currentNode.left)) {
        this.cameFromNode.put(this.currentNode.left, this.currentNode);
      }
      this.currentNode = this.currentNode.left;
      this.currentNode.changeColor(Color.ORANGE);
    }

    if (move.equals("right") && this.currentNode.right != null) {
      this.currentNode.changeColor(Color.BLUE);
      if (!this.cameFromNode.containsKey(this.currentNode.right)) {
        this.cameFromNode.put(this.currentNode.right, this.currentNode);
      }
      this.currentNode = this.currentNode.right;
      this.currentNode.changeColor(Color.ORANGE);
    }

    if (this.currentNode.equals(this.board.get(this.height - 1).get(this.length - 1))) {
      this.finished = true;
    }
  }

  // EFFECT: resets the maze and generates a new one 
  void newMaze() {
    this.board = new ArrayList<ArrayList<Node>>();
    this.edges = new ArrayList<Edge>();
    this.edgesInTheMaze = new ArrayList<Edge>();
    this.currentNode = null;
    this.animatedNodes = new ArrayList<Node>();
    this.visitedNodes = new ArrayList<Node>();
    this.cameFromNode = new HashMap<Node, Node>();
    this.finished = false;
    this.toggle = false;
    this.toggleList = new ArrayList<Node>();
    this.solved = false;
    this.wrongMoves = 0;
    this.createMaze();  
  }

  // EFFECT: override for big bang in order to allow users to do certain actions
  public void onKeyEvent(String s) {
    if (s.equals("b")) {
      this.searchHelp(this.board.get(0).get(0), 
          this.board.get(this.height - 1).get(this.length - 1), new Queue<Node>());
    }
    if (s.equals("d")) {
      this.searchHelp(this.board.get(0).get(0), 
          this.board.get(this.height - 1).get(this.length - 1), new Stack<Node>());
    }
    if (s.equals("up")) {
      this.manualEntry(s);
    }
    if (s.equals("down")) {
      this.manualEntry(s);
    }
    if (s.equals("left")) {
      this.manualEntry(s);
    }
    if (s.equals("right")) {
      this.manualEntry(s);
    }
    if (s.equals("r")) {
      this.newMaze();
    }
    // toggles the view of the visited paths
    if (s.equals("t")) {
      this.toggle = !this.toggle;
      if (this.toggle) {
        for (int y = 0; y < this.height; y++) {
          for (int x = 0; x < this.length; x++) {
            if (this.board.get(y).get(x).color.equals(Color.BLUE)) {
              this.toggleList.add(this.board.get(y).get(x));
              this.board.get(y).get(x).changeColor(Color.WHITE);
            }
          }
        }
      }
      else {
        for (int i = 0; i < this.toggleList.size(); i++) {
          this.toggleList.get(i).changeColor(Color.BLUE);
        }
        this.toggleList = new ArrayList<Node>();
      }
    }
  }

  // EFFECT: draws the maze and checks to see if it has been completed
  public void onTick() {
    if (this.visitedNodes.size() > this.animatedNodes.size()) {
      Node temp = this.visitedNodes.get(this.animatedNodes.size());
      temp.changeColor(Color.BLUE);
      this.animatedNodes.add(temp);
    } 
    else if (this.finished) {
      this.directPath();
      for (int y = 0; y < this.height; y++) {
        for (int x = 0; x < this.length; x++) {
          if (this.board.get(y).get(x).color.equals(Color.BLUE)) {
            this.wrongMoves++;
          }
        }
      }
    }
  }

  // method for big bang to draw 
  public WorldScene makeScene() {
    WorldScene maze = getEmptyScene();
    int sizeRect = 20;
    // has the starting x value as 25 and sets it to be more dynamic if the maze is small enough
    // this is done because if the maze is too big, it starts going negative and reverses the 
    // maze out of bounds. So if it is going we just keep it to be at the very left. 
    int dynamicPlaceX = 25;
    if (this.length * sizeRect < 750) {
      dynamicPlaceX = 750 - this.length * sizeRect;
    }
    int dynamicPlaceY = 25;


    // places the begin here green square
    this.board.get(0).get(0).changeColor(Color.GREEN);

    // places the end here purple square
    this.board.get(this.height - 1).get(this.length - 1).changeColor(Color.MAGENTA);


    // goes through the entire board, drawing each node as it goes
    for (int y = 0; y < this.height; y++) {
      for (int x = 0; x < this.length; x++) {
        Node temp = this.board.get(y).get(x);
        maze.placeImageXY(temp.drawNode(), x * temp.size + dynamicPlaceX, 
            y * temp.size + dynamicPlaceY);
      }
    }

    if (this.solved) {
      maze.placeImageXY(new TextImage("The maze has been solved. There were  " 
          + this.wrongMoves + " wrong moves.", 15, FontStyle.BOLD, Color.RED),
          this.length * sizeRect / 2 + dynamicPlaceX, this.height * sizeRect / 2 + dynamicPlaceY);
    }

    return maze;
  }
}

// class for testing 
class ExampleMaze extends World {
  Maze maze0;
  Maze maze1;
  Maze maze2;
  Maze maze3;
  Maze maze4;
  Maze maze5;
  Maze maze6;

  Edge edge1;
  Edge edge2;
  Edge edge3;

  Node node1;
  Node node2;
  Node node3;
  Node node4;

  Node maze3node0;
  Node maze3node1;
  Node maze3node2;
  Node maze3node3;

  HashMap<Integer, Integer> hash1;
  HashMap<Integer, Integer> hash2;

  Stack<Node> stack1;
  Queue<Node> queue1;

  ArrayList<Edge> maze3EdgeList = new ArrayList<Edge>(Arrays.asList(
      new Edge(854, this.maze3node0, this.maze3node1), 
      new Edge(92, this.maze3node2, this.maze3node3),
      new Edge(474, this.maze3node0, this.maze3node2), 
      new Edge(424, this.maze3node1, this.maze3node3)));

  // initializes the hashmaps for testing
  void initDataFind() {
    hash1 = new HashMap<Integer, Integer>();
    hash2 = new HashMap<Integer, Integer>();
  }

  // initializes the data for testing
  void initData() {
    this.maze0 = new Maze(1, 1, new Random(5));
    this.maze1 = new Maze(5, 5, new Random(5));
    this.maze2 = new Maze(50, 50, new Random(5));
    this.maze3 = new Maze(2, 2, new Random(5));
    this.maze4 = new Maze(3, 3, new Random(5));
    this.maze5 = new Maze(100, 60, new Random(5));
    this.maze6 = new Maze(6, 6, new Random(5));

    this.maze3node0 = new Node(0, null, null, null, this.maze3node2, Color.WHITE);
    this.maze3node1 = new Node(1, null, null, null, this.maze3node3, Color.WHITE);
    this.maze3node2 = new Node(2, null, null, this.maze3node3, null, Color.WHITE);
    this.maze3node3 = new Node(3, this.maze3node1, null, null, null, Color.WHITE);

    this.node1 = new Node(1);
    this.node2 = new Node(2, null, this.node1, null, null, Color.WHITE);
    this.node3 = new Node(3, null, this.node2, this.node1, null, Color.WHITE);
    this.node4 = new Node(4, this.node1, this.node2, this.node3, this.node1, Color.WHITE);

    this.edge1 = new Edge(100, this.node1, this.node2);
    this.edge2 = new Edge(0, this.node2, this.node3);
    this.edge3 = new Edge(50, this.node3, this.node4);

    this.stack1 = new Stack<Node>();
    this.queue1 = new Queue<Node>();
  }

  public WorldScene makeScene() {
    return getEmptyScene();
  }

  WorldImage node1Draw() {
    WorldImage whiteNode = new RectangleImage(10, 10, OutlineMode.SOLID, Color.WHITE);
    WorldImage verticalLineImage = 
        new LineImage(new Posn(0, 10), Color.BLACK);
    WorldImage horizontalLineImage = 
        new LineImage(new Posn(10, 0), Color.BLACK);

    whiteNode = whiteNode.movePinhole(-1 * 10 / 2, 0);
    whiteNode = new OverlayImage(verticalLineImage, whiteNode);

    whiteNode = whiteNode.movePinhole(10, 0);
    whiteNode = new OverlayImage(verticalLineImage, whiteNode);

    whiteNode = whiteNode.movePinhole(-1 * 10 / 2, -1 * 10 / 2);
    whiteNode = new OverlayImage(horizontalLineImage, whiteNode);

    whiteNode = whiteNode.movePinhole(0, 10);
    whiteNode = new OverlayImage(horizontalLineImage, whiteNode);

    return whiteNode;
  }

  WorldImage node3Draw() {
    WorldImage whiteNode = new RectangleImage(10, 10, OutlineMode.SOLID, Color.WHITE);
    WorldImage verticalLineImage = 
        new LineImage(new Posn(0, 10), Color.BLACK);
    WorldImage horizontalLineImage = 
        new LineImage(new Posn(10, 0), Color.BLACK);

    whiteNode = whiteNode.movePinhole(-1 * 10 / 2, 0);

    whiteNode = whiteNode.movePinhole(10, 0);

    whiteNode = whiteNode.movePinhole(-1 * 10 / 2, -1 * 10 / 2);
    whiteNode = new OverlayImage(horizontalLineImage, whiteNode);

    whiteNode = whiteNode.movePinhole(0, 10);
    whiteNode = new OverlayImage(horizontalLineImage, whiteNode);

    return whiteNode;
  }
  
  WorldScene drawMaze0() {
    WorldScene maze = this.makeScene();
    int height = 1;
    int length = 1;
    Node node0 = new Node(0);
    int dynamicPlaceX = 750 - 15 * length;
    int dynamicPlaceY = 20;
    int sizeRect = 10;
    ArrayList<ArrayList<Node>> board = new ArrayList<ArrayList<Node>>(
        Arrays.asList(new ArrayList<Node>(Arrays.asList(node0))));

    // goes through the entrie board, drawing each node as it goes
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < length; x++) {
        Node temp = board.get(y).get(x);
        sizeRect = temp.size;
        maze.placeImageXY(temp.drawNode(), x * temp.size + dynamicPlaceX, 
            y * temp.size + dynamicPlaceY);
      }
    }

    // places end square as this square overlaps the start square in this instance
    board.get(0).get(0).changeColor(Color.MAGENTA);

    return maze;
  }

  WorldScene drawMaze3() {
    WorldScene maze = this.makeScene();
    int height = 2;
    int length = 2;
    Node node0 = new Node(0);
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    Node node3 = new Node(3);
    node0.changeBottom(node2);
    node1.changeBottom(node3);
    node2.changeTop(node0);
    node2.changeRight(node3);
    node3.changeLeft(node2);
    node3.changeTop(node1);
    ArrayList<ArrayList<Node>> board = new ArrayList<ArrayList<Node>>(Arrays.asList(
        new ArrayList<Node>(Arrays.asList(node0, node1)), 
        new ArrayList<Node>(Arrays.asList(node2, node3))));
    int dynamicPlaceX = 750 - 15 * length;
    int dynamicPlaceY = 20;
    int sizeRect = 10;

    // goes through the entrie board, drawing each node as it goes
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < length; x++) {
        Node temp = board.get(y).get(x);
        sizeRect = temp.size;
        maze.placeImageXY(temp.drawNode(), x * temp.size + dynamicPlaceX, 
            y * temp.size + dynamicPlaceY);
      }
    }

    // places the begin here green square
    board.get(0).get(0).changeColor(Color.GREEN);

    // places the end square
    board.get(1).get(1).changeColor(Color.MAGENTA);

    return maze;
  }

  WorldScene drawMaze3_2() {
    WorldScene maze = this.makeScene();
    int height = 2;
    int length = 2; 
    Node node0 = new Node(0);
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    Node node3 = new Node(3);
    node0.changeRight(node1);
    node1.changeLeft(node0);
    node1.changeBottom(node3);
    node2.changeRight(node3);
    node3.changeLeft(node2);
    node3.changeTop(node1);

    ArrayList<ArrayList<Node>> board = new ArrayList<ArrayList<Node>>(Arrays.asList(
        new ArrayList<Node>(Arrays.asList(node0, node1)), 
        new ArrayList<Node>(Arrays.asList(node2, node3))));
    int dynamicPlaceX = 750 - 15 * length;
    int dynamicPlaceY = 20;
    int sizeRect = 10;

    // goes through the entrie board, drawing each node as it goes
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < length; x++) {
        Node temp = board.get(y).get(x);
        sizeRect = temp.size;
        maze.placeImageXY(temp.drawNode(), x * temp.size + dynamicPlaceX, 
            y * temp.size + dynamicPlaceY);
      }
    }

    board.get(0).get(0).changeColor(Color.GREEN);
    board.get(1).get(1).changeColor(Color.MAGENTA);

    return maze;
  }

  WorldScene drawMaze4() {
    WorldScene maze = this.maze0.getEmptyScene();
    int length = 3;
    int height = 3;
    int sizeRect = 10;
    int dynamicPlaceX = 750 - 15 * sizeRect;
    int dynamicPlaceY = 20;

    Node node0 = new Node(0);
    Node node1 = new Node(1);
    Node node2 = new Node(2);
    Node node3 = new Node(3);
    Node node4 = new Node(4);
    Node node5 = new Node(5);
    Node node6 = new Node(6);
    Node node7 = new Node(7);
    Node node8 = new Node(8);

    node0.changeRight(node1);
    node1.changeLeft(node0);
    node1.changeBottom(node4);
    node1.changeRight(node2);
    node2.changeLeft(node1);
    node3.changeBottom(node6);
    node3.changeRight(node4);
    node4.changeTop(node1);
    node4.changeLeft(node3);
    node4.changeRight(node5);
    node5.changeLeft(node4);
    node6.changeTop(node3);
    node6.changeRight(node7);
    node7.changeLeft(node6);
    node7.changeRight(node8);
    node8.changeLeft(node7);

    ArrayList<ArrayList<Node>> board = new ArrayList<ArrayList<Node>>(Arrays.asList(
        new ArrayList<Node>(Arrays.asList(node0, node1, node2)), 
        new ArrayList<Node>(Arrays.asList(node3, node4, node5)), 
        new ArrayList<Node>(Arrays.asList(node6, node7, node8))));


    // goes through the entire board, drawing each node as it goes
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < length; x++) {
        Node temp = board.get(y).get(x);
        maze.placeImageXY(temp.drawNode(), x * temp.size + dynamicPlaceX, 
            y * temp.size + dynamicPlaceY);
      }
    }

    // places the begin here green square
    board.get(0).get(0).changeColor(Color.GREEN);

    // places the end square
    board.get(1).get(1).changeColor(Color.MAGENTA);

    return maze;
  }

  void testChangeLeft(Tester t) {
    this.initData();
    this.node1.changeLeft(this.node2);
    this.node2.changeLeft(this.node3);
    this.node3.changeLeft(this.node2);
    t.checkExpect(this.node1.left, this.node2);
    t.checkExpect(this.node2.left, this.node3);
    t.checkExpect(this.node3.left, this.node2);
  }

  void testChangeTop(Tester t) {
    this.initData();
    this.node1.changeTop(this.node2);
    this.node2.changeTop(this.node3);
    this.node3.changeTop(this.node2);
    t.checkExpect(this.node1.top, this.node2);
    t.checkExpect(this.node2.top, this.node3);
    t.checkExpect(this.node3.top, this.node2);
  }

  void testChangeRight(Tester t) {
    this.initData();
    this.node1.changeRight(this.node2);
    this.node2.changeRight(this.node3);
    this.node3.changeRight(this.node2);
    t.checkExpect(this.node1.right, this.node2);
    t.checkExpect(this.node2.right, this.node3);
    t.checkExpect(this.node3.right, this.node2);
  }

  void testChangeBottom(Tester t) {
    this.initData();
    this.node1.changeBottom(this.node2);
    this.node2.changeBottom(this.node3);
    this.node3.changeBottom(this.node2);
    t.checkExpect(this.node1.bottom, this.node2);
    t.checkExpect(this.node2.bottom, this.node3);
    t.checkExpect(this.node3.bottom, this.node2);
  }

  void testChangeColor(Tester t) {
    this.initData();
    this.node1.changeColor(Color.BLUE);
    this.node2.changeColor(Color.WHITE);
    this.node3.changeColor(Color.BLACK);
    t.checkExpect(this.node1.color, Color.BLUE);
    t.checkExpect(this.node2.color, Color.WHITE);
    t.checkExpect(this.node3.color, Color.BLACK);
  }

  void testDrawNode(Tester t) {
    this.initData();
    t.checkExpect(this.node1.drawNode(), this.node1Draw());
    t.checkExpect(this.node4.drawNode(), new RectangleImage(
        10, 10, OutlineMode.SOLID, this.node4.color).movePinhole(0, 5));
    t.checkExpect(this.node3.drawNode(), this.node3Draw());
  }

  void testEdgeComparatorCompare(Tester t) {
    this.initData();
    t.checkExpect(new EdgeComparator().compare(this.edge1, this.edge2), 100);
    t.checkExpect(new EdgeComparator().compare(this.edge1, this.edge3), 50);
    t.checkExpect(new EdgeComparator().compare(this.edge2, this.edge3), -50);
    t.checkExpect(new EdgeComparator().compare(this.edge1, this.edge1), 0);
  }

  void testCreateMaze(Tester t) {
    this.initData();
    // this.maze.createMaze() is in the constructor so it is already run
    // when this.initData() is called

    // checking that the length and height are appropriately initialized
    t.checkExpect(this.maze1.length * this.maze1.height, 25);
    t.checkExpect(this.maze2.length * this.maze2.height, 2500);
    t.checkExpect(this.maze3.length * this.maze3.height, 4);

    // checking that the number of edges in the maze is as expected
    // edgesInTheMaze represents the edges that remain after running Kruskal's on it
    // edgesInTheMaze will be equivalent to the number of nodes minus one
    t.checkExpect(this.maze2.edgesInTheMaze.size(), this.maze2.length * this.maze2.height - 1);
    t.checkExpect(this.maze3.edgesInTheMaze.size(), this.maze3.length * this.maze3.height - 1);
  }

  void testIsEmpty(Tester t) {
    this.initData();
    t.checkExpect(this.stack1.isEmpty(), true);
    t.checkExpect(this.queue1.isEmpty(), true);
    this.stack1.add(this.node1);
    this.queue1.add(this.node1);
    t.checkExpect(this.stack1.isEmpty(), false);
    t.checkExpect(this.queue1.isEmpty(), false);
  }

  void testAdd(Tester t) {
    this.initData();
    t.checkExpect(this.stack1.isEmpty(), true);
    t.checkExpect(this.queue1.isEmpty(), true);
    this.stack1.add(this.node1);
    this.queue1.add(this.node1);
    t.checkExpect(this.stack1.isEmpty(), false);
    t.checkExpect(this.queue1.isEmpty(), false);
    t.checkExpect(this.stack1.remove(), this.node1);
    t.checkExpect(this.queue1.remove(), this.node1);
  }

  void testRemove(Tester t) {
    this.initData();
    t.checkExpect(this.stack1.isEmpty(), true);
    t.checkExpect(this.queue1.isEmpty(), true);
    this.stack1.add(this.node1);
    this.queue1.add(this.node1);
    t.checkExpect(this.stack1.isEmpty(), false);
    t.checkExpect(this.queue1.isEmpty(), false);
    t.checkExpect(this.stack1.remove(), this.node1);
    t.checkExpect(this.queue1.remove(), this.node1);
  }

  void testSearchHelp(Tester t) {
    this.initData();
    this.initData();
    this.maze1.onKeyEvent("b");
    this.maze3.onKeyEvent("b");
    t.checkExpect(this.maze1.visitedNodes.size(), 23);
    t.checkExpect(this.maze3.visitedNodes.size(), 3);
    this.maze1.newMaze();
    this.maze3.newMaze();
    this.maze1.onKeyEvent("d");
    this.maze3.onKeyEvent("d");
    t.checkExpect(this.maze1.visitedNodes.size(), 9);
    t.checkExpect(this.maze3.visitedNodes.size(), 3);
  }

  void testDirectPath(Tester t) {
    this.initData();
    // checks to see the direct path is not there before it is instantiated
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.WHITE);
    t.checkExpect(this.maze4.board.get(1).get(1).color, Color.WHITE);
    t.checkExpect(this.maze4.board.get(1).get(2).color, Color.WHITE);

    // instantiates the direct path
    this.maze4.onKeyEvent("b");
    this.maze4.directPath();

    // checks to see if the direct path is present
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.YELLOW);
    t.checkExpect(this.maze4.board.get(1).get(1).color, Color.YELLOW);
    t.checkExpect(this.maze4.board.get(1).get(2).color, Color.YELLOW);

    // checks to see the direct path is not there before it is instantiated
    t.checkExpect(this.maze6.board.get(0).get(1).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(2).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(3).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(4).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(1).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(2).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(3).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(4).get(5).color, Color.WHITE);

    // instantiates the direct path
    this.maze6.onKeyEvent("b");
    this.maze6.directPath();    

    // checks to see the direct path is not there before it is instantiated
    t.checkExpect(this.maze6.board.get(0).get(1).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(2).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(3).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(4).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(1).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(2).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(3).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(4).get(5).color, Color.YELLOW);
  }

  void testManualEntry(Tester t) {
    this.initData();

    // checks to see if movement works, if the trail is present, and if the current node is present
    this.maze4.onKeyEvent("right");
    this.maze4.onKeyEvent("right");
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.BLUE);
    t.checkExpect(this.maze4.board.get(0).get(2).color, Color.ORANGE);
    t.checkExpect(this.maze4.board.get(0).get(2), this.maze4.currentNode);

    this.maze4.onKeyEvent("left");
    t.checkExpect(this.maze4.board.get(0).get(2).color, Color.BLUE);
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.ORANGE);
    t.checkExpect(this.maze4.board.get(0).get(1), this.maze4.currentNode);


    // checks to see if the condition is met when the node reaches the
    // end of the maze manually
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    t.checkExpect(this.maze6.finished, true);
  }

  void testNewMaze(Tester t) {
    this.initData();
    t.checkExpect(this.maze3.makeScene(), this.drawMaze3());
    this.maze3.onKeyEvent("r");
    t.checkExpect(this.maze3.makeScene(), this.drawMaze3_2());
  }

  void testOnKeyEvent(Tester t) {
    // onKeyEvent is tested thoroughly through the other test methods that require onKeyEvent
    // to initialize the status of the maze so that the testing for that status can be done.
    // Because testing onKeyEvent would be the same thing as testing those methods more or less, 
    // we ask you to refer to the other methods for testing of onKeyEvent's functionality. 
  }

  void testOnTick(Tester t) {
    this.initData();

    // checks to see the direct path is not there before the tick
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.WHITE);
    t.checkExpect(this.maze4.board.get(1).get(1).color, Color.WHITE);
    t.checkExpect(this.maze4.board.get(1).get(2).color, Color.WHITE);

    // moving the current node to the end of the maze
    this.maze4.onKeyEvent("right");
    this.maze4.onKeyEvent("down");
    this.maze4.onKeyEvent("right");
    this.maze4.onKeyEvent("down");
    this.maze4.onTick();

    // checks to see the direct path is there after the tick
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.YELLOW);
    t.checkExpect(this.maze4.board.get(1).get(1).color, Color.YELLOW);
    t.checkExpect(this.maze4.board.get(1).get(2).color, Color.YELLOW);


    // checks to see the direct path is not there before the tick
    t.checkExpect(this.maze6.board.get(0).get(1).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(2).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(3).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(4).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(0).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(1).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(2).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(3).get(5).color, Color.WHITE);
    t.checkExpect(this.maze6.board.get(4).get(5).color, Color.WHITE);

    // moving the current node to the end of the maze
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("right");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onKeyEvent("down");
    this.maze6.onTick();

    // checks to see the direct path is not there before the tick
    t.checkExpect(this.maze6.board.get(0).get(1).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(2).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(3).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(4).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(0).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(1).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(2).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(3).get(5).color, Color.YELLOW);
    t.checkExpect(this.maze6.board.get(4).get(5).color, Color.YELLOW);
  }

  void testNumberOfWrongMoves(Tester t) {
    this.initData();
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("left");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("right");
    this.maze1.makeScene();
    this.maze1.onTick();
    t.checkExpect(this.maze1.wrongMoves, 1);
    this.maze3.onKeyEvent("down");
    this.maze3.onKeyEvent("right");
    this.maze3.onKeyEvent("right");
    this.maze3.makeScene();
    this.maze3.onTick();
    t.checkExpect(this.maze3.wrongMoves, 0);
    // testing to make sure that the wrong moves is calculated at the end as expected
    this.maze2.onKeyEvent("right");
    this.maze2.onKeyEvent("right");
    this.maze2.makeScene();
    this.maze2.onTick();
    t.checkExpect(this.maze2.wrongMoves, 0);
  }

  void testToggle(Tester t) {
    this.initData();

    // moves the current node to the end of the maze, creating extra moves so a
    // trail is present when the direct path is drawn
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("left");
    this.maze1.onKeyEvent("down");
    this.maze1.onKeyEvent("right");
    this.maze1.onKeyEvent("right");
    this.maze1.onTick();
    this.maze1.makeScene();
    t.checkExpect(this.maze1.board.get(3).get(3).color, Color.BLUE);
    t.checkExpect(this.maze1.toggleList.size(), 0);
    this.maze1.onKeyEvent("t");
    t.checkExpect(this.maze1.toggleList.size(), 1);
    t.checkExpect(this.maze1.toggleList.get(0).color, Color.WHITE);
    this.maze1.onKeyEvent("t");
    t.checkExpect(this.maze1.toggleList.size(), 0);

    this.maze4.onKeyEvent("right");
    this.maze4.onKeyEvent("right");
    t.checkExpect(this.maze4.board.get(0).get(1).color, Color.BLUE);
    t.checkExpect(this.maze4.toggleList.size(), 0);
    this.maze4.onKeyEvent("t");
    t.checkExpect(this.maze4.toggleList.size(), 2);
    t.checkExpect(this.maze4.toggleList.get(0).color, Color.WHITE);
    this.maze4.onKeyEvent("t");
    t.checkExpect(this.maze4.toggleList.size(), 0);
  }

  void testmakeScene(Tester t) {
    this.initData();
    t.checkExpect(this.maze0.makeScene(), this.drawMaze0());
    t.checkExpect(this.maze3.makeScene(), this.drawMaze3());
    t.checkExpect(this.maze4.makeScene(), this.drawMaze4());
  }

  void testEdgesInMaze(Tester t) {
    this.initData();
    // because we cannot directly access the edgesInMaze() methods information, we check
    // what the output of the method is and make sure that it follows the formula that the size
    // of the edgesInTheMaze list is going to be 1 less than the length * the height of the maze
    t.checkExpect(this.maze0.edgesInTheMaze, new ArrayList<Edge>());
    t.checkExpect(this.maze3.edgesInTheMaze.size(), 3);
    t.checkExpect(this.maze5.edgesInTheMaze.size(), 5999);
  }

  void testFind(Tester t) {
    this.initDataFind();
    this.initData();
    hash1.put(1, 2);
    hash1.put(2, 3);
    hash1.put(3, 4);
    hash1.put(4, 5);
    hash1.put(5, 5);
    t.checkExpect(this.maze2.find(this.hash1, 1), 5);
    hash2.put(3, 5);
    hash2.put(4, 6);
    hash2.put(5, 5);
    t.checkExpect(this.maze2.find(this.hash2, 3), 5);
  }

  void testBigBang(Tester t) {
    this.maze1.bigBang(1500, 1500, 0.1);
    //this.maze2.bigBang(1500, 1500, 0.1);
    //this.maze3.bigBang(1500, 1500, 0.1);
    //this.maze4.bigBang(1500, 1500, 0.1);
    //this.maze5.bigBang(2000, 2000, .00001);
    //this.maze6.bigBang(1500, 1500, 0.1);
  }
}