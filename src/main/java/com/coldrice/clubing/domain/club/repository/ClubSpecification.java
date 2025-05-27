package com.coldrice.clubing.domain.club.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import com.coldrice.clubing.domain.club.dto.ClubSearchRequest;
import com.coldrice.clubing.domain.club.entity.Club;
import com.coldrice.clubing.domain.club.entity.ClubStatus;
import com.coldrice.clubing.domain.club.entity.ClubType;

import jakarta.persistence.criteria.Predicate;

public class ClubSpecification {
	public static Specification<Club> search(ClubSearchRequest request) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// 기본 조건: 승인된 동아리만
			predicates.add(cb.equal(root.get("status"), ClubStatus.APPROVED));
			predicates.add(cb.equal(root.get("type"), ClubType.동아리));

			// 단일 query로 name, category, keyword, description 포함 여부 검사
			if (StringUtils.hasText(request.query())) {
				String likeQuery = "%" + request.query().toLowerCase() + "%";

				predicates.add(cb.or(
					cb.like(cb.lower(root.get("name")), likeQuery),
					cb.like(cb.lower(root.get("category")), likeQuery),
					cb.like(cb.lower(root.get("keyword")), likeQuery),
					cb.like(cb.lower(root.get("description")), likeQuery)
				));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}

