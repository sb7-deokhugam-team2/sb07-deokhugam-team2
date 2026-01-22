package com.deokhugam.domain.popularbook.repository;

import com.deokhugam.domain.popularbook.entity.PopularBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PopularBookRepository extends JpaRepository<PopularBook, UUID>, PopularBookRepositoryCustom {

}
