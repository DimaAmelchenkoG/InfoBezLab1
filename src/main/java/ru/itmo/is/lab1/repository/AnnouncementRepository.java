package ru.itmo.is.lab1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.itmo.is.lab1.domain.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

	List<Announcement> findAllByOrderByIdAsc();

	@Query("""
			select a from Announcement a
			where lower(a.title) like lower(concat('%', :query, '%'))
			   or lower(a.body) like lower(concat('%', :query, '%'))
			order by a.id
			""")
	List<Announcement> searchByQuery(@Param("query") String query);

}
