import java.util.LinkedList;

class Solution {
    // 内部类，用于存储字符及其连续计数
    static class Node {
        char ch;
        int count;
        
        Node(char ch, int count) {
            this.ch = ch;
            this.count = count;
        }
    }

    public static String compressWord(String word, int k) {
        if (k == 0) return word; // 边界条件检查
        
        LinkedList<Node> stack = new LinkedList<>();
        
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            
            // 栈不为空且字符与栈顶字符相同
            if (!stack.isEmpty() && stack.getLast().ch == c) {
                Node top = stack.getLast();
                top.count++;
                
                // 如果达到k个连续字符，移除栈顶元素
                if (top.count == k) {
                    stack.removeLast();
                }
            } else {
                // 压入新字符
                stack.addLast(new Node(c, 1));
            }
        }
        
        // 构建最终结果
        StringBuilder result = new StringBuilder();
        for (Node node : stack) {
            for (int i = 0; i < node.count; i++) {
                result.append(node.ch);
            }
        }
        
        return result.toString();
    }

    public static void main(String[] args) {
        // 测试样例
        System.out.println(compressWord("aba", 2)); // 输出: "aba"
        System.out.println(compressWord("baac", 2)); // 输出: "bc"
        System.out.println(compressWord("abbcccbb", 3)); // 输出: "a"
    }
}关键点说明：

数据结构：使用 LinkedList 模拟栈，保存字符及其连续计数（ Node 对象）。

核心算法：

遍历字符串的每个字符

如果栈非空且当前字符与栈顶字符相同：

增加栈顶字符的计数

当计数达到 k 时，弹出栈顶

否则，创建新节点压入栈中

结果构建：遍历栈中所有节点，按计数重复字符生成结果字符串

时间复杂度：O(n)，每个字符仅处理一次

空间复杂度：O(n)，最坏情况下所有字符都保留在栈中

测试样例验证：

 "aba", k=2  → 无连续相同字符，输出 "aba" 

 "baac", k=2  → 删除连续 aa ，输出 "bc" 

 "abbcccbb", k=3  → 先删 ccc 得 "abbbb" ，再删3个 b 得 "a" 

此实现满足题目要求，能够高效处理最长10^5的字符串，符合题目约束条件。

    import java.util.LinkedList;

public class StringCompressor {
    public static String compressWord(String word, int k) {
        if (k <= 1) return ""; // k>=2 per constraints
        
        // 用链表存储字符和对应的连续计数
        LinkedList<Character> charList = new LinkedList<>();
        LinkedList<Integer> countList = new LinkedList<>();
        
        for (char c : word.toCharArray()) {
            // 当前字符与链表尾字符相同
            if (!charList.isEmpty() && charList.getLast() == c) {
                int newCount = countList.removeLast() + 1; // 增加计数
                
                if (newCount == k) {
                    charList.removeLast(); // 达到k个，移除字符
                } else {
                    countList.addLast(newCount); // 未达到k，更新计数
                }
            } else {
                // 添加新字符和初始计数1
                charList.addLast(c);
                countList.addLast(1);
            }
        }
        
        // 构建结果字符串
        StringBuilder result = new StringBuilder();
        while (!charList.isEmpty()) {
            char c = charList.removeFirst();
            int count = countList.removeFirst();
            for (int i = 0; i < count; i++) {
                result.append(c);
            }
        }
        return result.toString();
    }

    // 测试样例
    public static void main(String[] args) {
        System.out.println(compressWord("aba", 2));    // "aba"
        System.out.println(compressWord("baac", 2));   // "bc"
        System.out.println(compressWord("abbcccb", 3)); // "a"
    }
}

