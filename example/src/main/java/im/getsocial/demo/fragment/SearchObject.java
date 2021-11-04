package im.getsocial.demo.fragment;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class SearchObject {

    public String searchTerm;
    @Nullable
    public List<String> labels;
    @Nullable
    public Map<String, String> properties;

    public static SearchObject empty() {
        SearchObject obj = new SearchObject();
        obj.searchTerm = "";
        return obj;
    }
}
