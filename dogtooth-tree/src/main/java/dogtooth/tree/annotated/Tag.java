package dogtooth.tree.annotated;

import java.util.HashSet;
import java.util.Set;

public class Tag {
    final private Set<String> m_tag;
    
    public static Tag tag(String...tags) {
        return new Tag(tags);
    }
    
    public Tag(String... tag) {
        m_tag = new HashSet<String>();
        for (String t : tag) {
            m_tag.add( t );
        }
    }
    
    public boolean contains(Tag other) {
        return m_tag.contains( other );
    }
    
    public String toString() {
        return m_tag.toString();
    }
}
