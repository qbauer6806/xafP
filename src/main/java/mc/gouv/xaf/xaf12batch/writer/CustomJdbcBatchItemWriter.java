package mc.gouv.xaf.xaf12batch.writer;

import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.dao.EmptyResultDataAccessException;

public class CustomJdbcBatchItemWriter<T> implements ItemWriter<T> {

    private final JdbcBatchItemWriter<T> delegate;

    public CustomJdbcBatchItemWriter(JdbcBatchItemWriter<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(List<? extends T> items) throws Exception {
        for (T item : items) {
            try {
                delegate.write(List.of(item)); // Write each item individually
            } catch (EmptyResultDataAccessException e) {
                // Log the exception and the item that failed to update
                System.err.println("Item did not update any rows: " + item);
            }
        }
    }

    public static <T> CustomJdbcBatchItemWriter<T> customJdbcBatchItemWriter(JdbcBatchItemWriter<T> delegate) {
        return new CustomJdbcBatchItemWriter<>(delegate);
    }
}
