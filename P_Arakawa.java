import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;

public class P_Arakawa {
    private int numberOfPlayers;
    private int numberOfGames;
    private int numberOfSelectNodes; // 1ゲームで選択するノード
    private int numberOfNodes;
    private int numberOfEdges;
    private int patternSize;
    private int playerCode; // 0始まりの識別番号
    private int[][] edges;
    private int[][] weight;
    private Scanner sc;
    static final String playerName = "P_Arakawa";

    /**
     * 書き換え箇所．ノード選択のAI
     * 
     * @param record record[i][j]:ゲームiのノードjの獲得プレイヤーID．未獲得は-1
     * @param game   ゲーム数
     * @return 選択ノード番号
     */
    /*
    private int select(int[][][][] record, int game, int sequenceNumber, List<Integer> sequence,
            List<Integer>[] selectedNodeLists) {

        // ゲーム木を探索して最良手とその評価値を得る
        int[] nowbest = putBest(record, sequence, 0, PlayerDistance, minDistance, region, values); //TODO:
        return nowbest[1];
    }
    */

    
    //playerDistance, minDistance, region, valuesの初期化をどうするか
    //playerDistance -> calcPlayerDistanceで解決?
    //minDistance, region, valuesは
    /*
    private int select(int[][][][] record, int game, int sequenceNumber, List<Integer> sequence,
            List<Integer>[] selectedNodeLists) {

        // ゲーム木を探索して最良手とその評価値を得る
        int[][][] playerDistance = new int[numberOfPlayers][10][10];
        int[][] minDistance = new int[10][10];
        int[][] region = new int[10][10];
        int[] values = new int[numberOfPlayers];
        for(int i=0; i<numberOfPlayers; i++){
            values[i] = 0;
            for(int j=0; j<10; j++){
                for(int k=0; k<10; k++){
                    playerDistance[i][j][k] = 100;
                    minDistance[j][k] = 100;
                    region[j][k] = -1;
                }
            }
        }
        playerDistance = calcPlayerDistance(selectedNodeLists);
        region = calcRegion(playerDistance, minDistance);
        values = calcValue(region);

        int[] nowbest = putBest(record[game][sequenceNumber], sequence, 0, playerDistance, minDistance, region, values); // TODO:
        return nowbest[1];
    }
    */

    //minDistanceの初期化？？
    private int select(int[][][][] record, int game, int sequenceNumber, List<Integer> sequence,
        List<Integer>[] selectedNodeLists) {

        // ゲーム木を探索して最良手とその評価値を得る
        int nowbest = putNowBest(selectedNodeLists, record[game][sequenceNumber], playerCode); //TODO:
        return nowbest;
    }


    /**
     * 各プレイヤーの選択ノードのリストを与えると，各プレイヤー各ノードへの距離を計算して返す
     * 
     * @param selectedNodeLists
     * @return
     */
    private int[][][] calcPlayerDistance(List<Integer>[] selectedNodeLists) {
        int[][][] playerDistance = new int[numberOfPlayers][10][10];
        // 初期化
        for (int[][] array : playerDistance) {
            for (int[] array2 : array) {
                Arrays.fill(array2, 100);
            }
        }
        for (int i = 0; i < numberOfPlayers; i++) {
            for (int node : (List<Integer>) selectedNodeLists[i]) {
                int tmp_y = node / 10;
                int tmp_x = node % 10;
                for (int j = 0; j < 10; j++) {
                    for (int k = 0; k < 10; k++) {
                        int tmp_dis = Math.abs(j - tmp_x) + Math.abs(k - tmp_y);
                        if (tmp_dis < playerDistance[i][j][k])
                            playerDistance[i][j][k] = tmp_dis;
                    }
                }
            }
        }
        return playerDistance;
    }

    /**
     * playerCodeがnodeを獲得した時に，playerDistanceとregionを更新し，評価値を返す
     * 
     * @param playerValues
     * @param playerDistance
     * @param region
     * @param minDistance
     * @param playerCode
     * @param node
     * @param weight
     */
    private void update(int[][][] playerDistance, int[][] minDistance, int[][] region, int[] playerValues,
            int playerCode, int node) {
        int x = node % 10;
        int y = node / 10;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int distance = Math.abs(x - j) + Math.abs(y - i);
                // 距離が小さくなるなら
                if (distance < playerDistance[playerCode][i][j]) {
                    playerDistance[playerCode][i][j] = distance;

                    // 各プレイヤーの中で最小になるなら評価値を操作してregionを更新する
                    if (distance < minDistance[i][j]) {
                        minDistance[i][j] = distance;
                        if (region[i][j] != -1) {
                            playerValues[region[i][j]] -= weight[i][j];
                        }
                        playerValues[playerCode] += weight[i][j];
                        region[i][j] = playerCode;

                        // 同点
                    } else if (distance == minDistance[i][j] && region[i][j] != -1) {
                        playerValues[region[i][j]] -= weight[i][j];
                        region[i][j] = -1;
                    }
                }
            }
        }
    }

    /**
     * playerCodeがnodeを獲得した時に，playerDistanceとregionを更新し，評価値を返す
     * 
     * @param playerValues
     * @param playerDistance
     * @param region
     * @param minDistance
     * @param playerCode
     * @param node
     * @param weight
     */
    private int[] calcPutNodeValues(final int[] playerValues, final int[][][] playerDistance, final int[][] region,
            final int[][] minDistance, final int playerCode, final int node) {
        int[] newValues = playerValues.clone();
        int x = node % 10;
        int y = node / 10;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int distance = Math.abs(x - j) + Math.abs(y - i);
                // 距離が小さくなるなら
                if (distance < playerDistance[playerCode][i][j]) {
                    // 各プレイヤーの中で最小になるなら評価値を操作
                    if (distance < minDistance[i][j]) {
                        if (region[i][j] != -1) {
                            newValues[region[i][j]] -= weight[i][j];
                        }
                        newValues[playerCode] += weight[i][j];

                        // 同点
                    } else if (distance == minDistance[i][j] && region[i][j] != -1) {
                        newValues[region[i][j]] -= weight[i][j];
                    }
                }
            }
        }
        return newValues;
    }

    /**
     * 各プレイヤーの各ノードへの距離から，各ノードを獲得するプレイヤーのコードを返す
     * 
     * @param playerDistance
     * @return
     */
    private int[][] calcRegion(int[][][] playerDistance, int[][] minDistance) {
        int[][] region = new int[10][10];

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                minDistance[i][j] = playerDistance[0][i][j];
                int min_dis_player = 0;
                for (int k = 1; k < numberOfPlayers; k++) {
                    if (minDistance[i][j] > playerDistance[k][i][j]) {
                        minDistance[i][j] = playerDistance[k][i][j];
                        min_dis_player = k;
                    } else if (minDistance[i][j] == playerDistance[k][i][j]) {
                        min_dis_player = -1;
                    }
                    region[i][j] = min_dis_player;
                }
            }
        }
        return region;
    }

    /**
     * ノードの重みと各ノードの獲得プレイヤー情報から各プレイヤーのスコアを返す．
     * 
     * @param weight
     * @param region
     * @return
     */
    private int[] calcValue(int[][] region) {
        int[] value = new int[numberOfPlayers];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (region[i][j] != -1) {
                    value[region[i][j]] += weight[i][j];
                }
            }
        }
        return value;
    }

    /**
     * 各プレイヤーが選択したノードのリストを渡すと各プレイヤーの評価値が返ってくる
     * 
     * @param selectedNodeLists
     * @return
     */
    private int[] evaluate(List<Integer>[] selectedNodeLists) {

        // プレイヤーの距離計算
        int[][][] playerDistance = calcPlayerDistance(selectedNodeLists);

        // ノードの獲得状況の計算
        int[][] minDistance = new int[10][10];
        int[][] region = calcRegion(playerDistance, minDistance);

        // 評価値計算
        return calcValue(region);
    }

    /**
     * どれを選択すると最良なのかをO(n^2)で計算して選択するノードを返す 最良の定義は，自分とそれ以外のトップとの差の最大化
     * 
     * @param selectedNodeLists 選択したノードのリスト
     * @param record            レコード
     * @param playerCode        選択を考えるプレイヤーの識別番号
     * @return 選択ノード
     */
    
    private int putNowBest(List<Integer>[] selectedNodeLists, int[][] record, int playerCode) {
        // プレイヤーの距離計算
        int[][][] playerDistance = calcPlayerDistance(selectedNodeLists);
        int[][] minDistance = new int[10][10];
        
        // for (int i = 0; i < 10; i++) {
        //     for (int j = 0; j < 10; j++) {
        //         minDistance[i][j] = 100;
        //     }
        // }

        // ノードの獲得状況の計算
        int[][] region = calcRegion(playerDistance, minDistance);
        int max = -1000;
        int maxNode = -1;
        // 評価値計算
        int[] values = calcValue(region);
        for (int i = 0; i < numberOfNodes; i++) {
            if (record[i][0] != -1) {
                continue;
            }
            int[] tmpValues = calcPutNodeValues(values, playerDistance, region, minDistance, playerCode, i);
            int tmpObjective = diff(tmpValues, playerCode);
            // System.err.println("err:" + tmpObjective);
            if (tmpObjective > max) {
                max = tmpObjective;
                maxNode = i;
            }
        }
        // update(playerDistance, minDistance, region, values, playerCode, maxNode);
        return maxNode;
    }

    /**
     * 必要な情報がわかっている状況で最良のノードがどれかを返す
     * @param playerDistance
     * @param minDistance
     * @param region
     * @param values
     * @param record
     * @param playerCode
     * @return ノード番号
     */
    private int putNowBest(int[][][] playerDistance, int[][] minDistance, int[][] region, int[] values, int[][] record, int playerCode) {
        int max = -1000;
        int maxNode = -1;

        for (int i = 0; i < numberOfNodes; i++) {
            if (record[i][0] != -1) {
                continue;
            }
            int[] tmpValues = calcPutNodeValues(values, playerDistance, region, minDistance, playerCode, i);
            int tmpObjective = diff(tmpValues, playerCode);
            // System.err.println("err:" + tmpObjective);
            if (tmpObjective > max) {
                max = tmpObjective;
                maxNode = i;
            }
        }
        return maxNode;
    }

    
    // private int putNowBest(List<Integer>[] selectedNodeLists, int[][] record, int playerCode) {
    //     int max;
    //     int maxnode;
    //     int listsize = selectedNodeLists[playerCode].size();

    //     max = 0;
    //     maxnode = -1;

    //     for (int i = 0; i < 10; i++) {
    //         for (int j = 0; j < 10; j++) {
    //             int node = 10 * i + j;
    //             if (record[node][0] != -1) {
    //                 continue;
    //             }
    //             selectedNodeLists[playerCode].add(node);
    //             int[] tmp_value = evaluate(selectedNodeLists);
    //             if (tmp_value[playerCode] > max) {
    //                 max = tmp_value[playerCode];
    //                 maxnode = node;
    //             }
    //             selectedNodeLists[playerCode].remove(listsize);
    //         }
    //     }
    //     return maxnode;
    // }
    

    public int[] putBest(int[][] record, List<Integer> sequence, int turn, int[][][] playerDistance, int[][] minDistance, int[][] region, int[] values) {
        if(turn == numberOfSelectNodes){
            return new int[]{diff(values, this.playerCode), -1};
        }
        int best = -1000;
        int mySelectNode = -1;
        int k = 0;
        int[] selectNode = new int[numberOfPlayers];

        while (sequence.get(k) != playerCode) {
            int tmpnode = putNowBest(playerDistance, minDistance, region, values, record, sequence.get(k));
            update(playerDistance, minDistance, region, values, sequence.get(k), tmpnode);
            selectNode[sequence.get(k)] = tmpnode;
            record[tmpnode][0] = sequence.get(k);
            k++;
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int node = 10 * i + j;
                if (record[node][0] != -1) {
                    continue;
                }
                record[node][0] = playerCode;
                update(playerDistance, minDistance, region, values, playerCode, node);
                //selectNode[playerCode] = node;
                for (int s = k + 1; s < numberOfPlayers; s++) {
                    int tmpnode = putNowBest(playerDistance, minDistance, region, values, record, sequence.get(s));
                    update(playerDistance, minDistance, region, values, sequence.get(s), tmpnode);
                    selectNode[sequence.get(s)] = tmpnode;
                    record[node][0] = sequence.get(s);
                }
                int[] tmpbest = putBest(record, sequence, turn + 1, copyArray3D(playerDistance), copyArray2D(minDistance), copyArray2D(region), values.clone());
                int objectiveValue = diff(tmpbest, playerCode);
                if (best < objectiveValue) {
                    best = objectiveValue;
                    mySelectNode = node;
                }
                record[node][0] = -1;
                for (int s = k + 1; s < numberOfPlayers; s++) {
                    record[selectNode[sequence.get(s)]][0] = -1;
                }
            }
        }
        k = 0;
        while (sequence.get(k) != playerCode) {
            record[selectNode[sequence.get(k)]][0] = -1;
            k++;
        }
        
        return new int[]{best, mySelectNode};

    }

    /*
    public int[] putBest(List<Integer>[] selectedNodeLists, int[][] record, List<Integer> sequence, int turn) {
        if (turn == numberOfSelectNodes) {
            int objectiveValue = diff(evaluate(selectedNodeLists), playerCode);
            return new int[] { objectiveValue, -1 };
        }
        int best = -1000;
        int selectNode = -1;
        int k = 0;
        while (sequence.get(k) != playerCode) {
            int tmpnode = putNowBest(selectedNodeLists, record, sequence.get(k));
            selectedNodeLists[sequence.get(k)].add(tmpnode);
            record[tmpnode][0] = sequence.get(k);

            k++;
        }
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int node = 10 * i + j;
                if (record[node][0] != -1) {
                    continue;
                }
                selectedNodeLists[playerCode].add(node);
                record[node][0] = playerCode;
                for (int s = k + 1; s < numberOfPlayers; s++) {
                    int tmpnode = putNowBest(selectedNodeLists, record, sequence.get(s));
                    selectedNodeLists[sequence.get(s)].add(tmpnode);
                    record[node][0] = sequence.get(s);
                }
                int[] tmpbest = putBest(selectedNodeLists, record, sequence, turn + 1);
                int objectiveValue = diff(tmpbest, playerCode);
                if (best < objectiveValue) {
                    best = objectiveValue;
                    selectNode = node;
                }
                record[(int) selectedNodeLists[playerCode].remove(turn)][0] = -1;
                for (int s = k + 1; s < numberOfPlayers; s++) {
                    record[(int) selectedNodeLists[sequence.get(s)].remove(turn)][0] = -1;
                }
            }
        }
        k = 0;
        while (sequence.get(k) != playerCode) {
            record[(int) selectedNodeLists[sequence.get(k)].remove(turn)][0] = -1;
            k++;
        }

        return new int[] { best, selectNode };
    }
    */

    public int diff(int[] value, int playerCode) {
        int max = -1;
        for (int i = 0; i < numberOfPlayers; i++) {
            if (i == playerCode) {
                continue;
            }
            if (max < value[i]) {
                max = value[i];
            }
        }
        return value[playerCode] - max;
    }

    public int[][] copyArray2D(int[][] original){
        int[][] done = new int[original.length][];
        for(int i=0; i<original.length; i++){
            done[i] = original[i].clone();
        }
        return done;
    }

    public int[][][] copyArray3D(int[][][] original){
        int[][][] done = new int[original.length][][];

        for(int i=0; i<original.length; i++){
            done[i] = new int[original[i].length][];
            //System.err.println("i=" + i);
            //System.err.println("original[i].length=" + original[i].length);
            //System.err.println("done[i].length=" + done[i].length);
            for(int j=0; j<original[i].length; j++){
            //    System.err.println("j=" + j);
                done[i][j] = original[i][j].clone();
            }
        }
        return done;
    }

    public static void main(String[] args) {
        (new P_Arakawa()).run();
    }

    /**
     * ゲーム実行メソッド
     */
    @SuppressWarnings("unchecked")
    public void run() {
        sc = new Scanner(System.in);
        initialize();

        int[][][][] gameRecord = new int[numberOfGames][][][];

        // ゲーム数ループ
        for (int i = 0; i < numberOfGames; i++) {
            loadGraph();
            gameRecord[i] = new int[patternSize][numberOfNodes][2];
            for (int[][] sequenceRecord : gameRecord[i]) {
                for (int[] nodeInfo : sequenceRecord) {
                    Arrays.fill(nodeInfo, -1);
                }
            }
            for (int s = 0; s < patternSize; s++) {

                List<Integer> sequence = new LinkedList<Integer>();
                for (int j = 0; j < numberOfPlayers; j++) {
                    sequence.add(sc.nextInt());
                }
                List<Integer>[] selectedNodeLists = new ArrayList[numberOfPlayers];
                for (int j = 0; j < numberOfPlayers; j++) {
                    selectedNodeLists[j] = new ArrayList<Integer>();
                }
                // 選択ノード数分のループ
                for (int j = 0; j < numberOfSelectNodes; j++) {

                    for (int p : sequence) {
                        int selectNode;
                        if (p == playerCode) {
                            selectNode = select(gameRecord, i, s, sequence, selectedNodeLists);
                            System.out.println(selectNode);
                        } else {
                            selectNode = sc.nextInt();
                        }
                        gameRecord[i][s][selectNode][0] = p;
                        gameRecord[i][s][selectNode][1] = j;
                        selectedNodeLists[p].add(selectNode);
                    }
                }
            }
        }
    }

    /**
     * 初期化
     */
    private void initialize() {
        numberOfPlayers = sc.nextInt();
        numberOfGames = sc.nextInt();
        numberOfSelectNodes = sc.nextInt();
        patternSize = sc.nextInt();
        playerCode = sc.nextInt();
        System.out.println(playerName);
    }

    /**
     * グラフの読み込み ノード数，辺数，辺の情報（ノードA ノードB）の入力
     */
    private void loadGraph() {
        numberOfNodes = sc.nextInt();
        numberOfEdges = sc.nextInt();
        weight = new int[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                weight[i][j] = sc.nextInt();
            }
        }
        edges = new int[numberOfEdges][2];
        for (int i = 0; i < numberOfEdges; i++) {
            edges[i][0] = sc.nextInt();
            edges[i][1] = sc.nextInt();
        }
    }

}
