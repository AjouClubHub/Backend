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

			// 기본 필터: 승인된 클럽 & 동아리 타입
			predicates.add(cb.equal(root.get("status"), ClubStatus.APPROVED));
			predicates.add(cb.equal(root.get("type"), ClubType.동아리));

			// 카테고리 필터
			if (StringUtils.hasText(request.category())) {
				predicates.add(cb.equal(root.get("category"), request.category()));
			}

			// 키워드 포함 검색
			if (StringUtils.hasText(request.keyword())) {
				String kw = "%" + request.keyword() + "%";
				predicates.add(cb.or(
					cb.like(root.get("name"), kw),
					cb.like(root.get("keyword"), kw),
					cb.like(root.get("description"), kw)
				));
			}

			// 이름으로 부분 검색
			if (StringUtils.hasText(request.name())) {
				String nameKw = "%" + request.name() + "%";
				predicates.add(cb.like(root.get("name"), nameKw));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
