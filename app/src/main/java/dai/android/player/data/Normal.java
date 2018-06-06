package dai.android.player.data;

import java.util.ArrayList;

public class Normal {

    public String name;
    public short age;
    private ArrayList<String> abc = new ArrayList<>();

    public void makeArray() {
        abc.add("" + hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nname = ");
        sb.append(name);
        sb.append("\n");

        sb.append("name = ");
        sb.append(age);
        sb.append("\n");

        sb.append("abc = {\n");
        for (int i = 0; i < abc.size(); ++i) {
            sb.append("\t");
            sb.append(abc.get(i));
            sb.append("\n");
        }
        sb.append("}");

        return sb.toString();
    }
}
