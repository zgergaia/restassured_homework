import java.util.List;

public class Books {
    public List<Book> books;

    @Override
    public String toString() {
        StringBuilder _str = new StringBuilder();
        for (var i : books)
            _str.append(i.toString() + "\n");

        return _str.toString();
    }
}
