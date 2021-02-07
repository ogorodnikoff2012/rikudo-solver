To generate puzzles, you first should write a configuration file in XML format (see examples
in puzzles/*.xml). After that, you can run the program in the following way:

Build:

$ cd $PROJECT_ROOT
$ mkdir out
$ javac -cp lib/sat4j-sat.jar -d out \
    ./src/fr/polytechnique/rikudo/solver/AdjListGraph.java \
    ./src/fr/polytechnique/rikudo/solver/GraphReader.java \
    ./src/fr/polytechnique/rikudo/solver/ReducingToSATSolver.java \
    ./src/fr/polytechnique/rikudo/solver/Constraints.java \
    ./src/fr/polytechnique/rikudo/solver/BacktrackingSolver.java \
    ./src/fr/polytechnique/rikudo/solver/MatrixGraph.java \
    ./src/fr/polytechnique/rikudo/solver/IGraph.java \
    ./src/fr/polytechnique/rikudo/solver/IHamPathSolver.java \
    ./src/fr/polytechnique/rikudo/puzzle/RikudoPuzzle.java \
    ./src/fr/polytechnique/rikudo/binaryImages/TestBinaryImage.java \
    ./src/fr/polytechnique/rikudo/binaryImages/BinaryImage.java \
    ./src/fr/polytechnique/rikudo/hexagonal/PrettyPictures.java \
    ./src/fr/polytechnique/rikudo/hexagonal/GridGenerator.java \
    ./src/fr/polytechnique/rikudo/hexagonal/EisensteinInteger.java \
    ./src/fr/polytechnique/rikudo/hexagonal/GridGraph.java \
    ./src/fr/polytechnique/rikudo/hexagonal/Cell.java \
    ./src/fr/polytechnique/rikudo/examples/SATUser.java \
    ./src/fr/polytechnique/rikudo/benchmark/GraphBuilder.java \
    ./src/fr/polytechnique/rikudo/benchmark/GridPathCounter.java \
    ./src/fr/polytechnique/rikudo/benchmark/Benchmark.java

Run:

$ cd $PROJECT_ROOT
$ java -classpath out/production/rikudo-solver:lib/sat4j-sat.jar fr.polytechnique.rikudo.hexagonal.GridGenerator puzzles/bowtie.xml puzzles/christmasTree.xml