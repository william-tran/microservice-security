package microsec.freddysbbq.menu;

import org.springframework.data.repository.PagingAndSortingRepository;

import microsec.freddysbbq.menu.model.v1.MenuItem;

public interface MenuItemRepository extends PagingAndSortingRepository<MenuItem, Long> {
}