package mc.gouv.xaf.shared.dto;

import java.util.List;

/**
 * Copie de org.springframework.data.domain.Page pour ne pas avoir de dépendance avec Spring dans le client, et pour matcher les besoins front
 * Adaptation des noms pour la réponse de customRequest
 *
 * @param <T>
 * @author uek
 */
public class PageNew<T> {

    private long itemsCounter;

    private int page;

    private int itemsPerPage;

    private List<T> currentPageItems;

    /**
     * @return the total amount of elements
     */
    public long getItemsCounter() {
        return itemsCounter;
    }

    public void setItemsCounter(long itemsCounter) {
        this.itemsCounter = itemsCounter;
    }

    /**
     * @return the number of the current {@link org.springframework.data.domain.Slice}.
     */
    public int getPage() {
        return this.page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the size of the {@link org.springframework.data.domain.Slice}.
     */
    public int getItemsPerPage() {
        return this.itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * @return the page content as {@link List}.
     */
    public List<T> getCurrentPageItems() {
        return currentPageItems;
    }

    public void setCurrentPageItems(List<T> currentPageItems) {
        this.currentPageItems = currentPageItems;
    }

}
