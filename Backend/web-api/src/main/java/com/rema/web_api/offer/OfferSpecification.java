package com.rema.web_api.offer;

import com.rema.web_api.offer.dto.OfferMapPointRequest;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OfferSpecification {
    public static Specification<Offer> offerFilters(OfferMapPointRequest req) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getMarket() != null) {
                predicates.add(cb.equal(root.get("market"), req.getMarket()));
            }
            addRange(predicates, cb, root.get("pricePln"), req.getPricePlnFrom(), req.getPricePlnTo());

            if (req.getPm2From() != null || req.getPm2To() != null) {

                Expression<Float> pricePerM2 = cb.quot(
                        root.get("pricePln").as(Float.class),
                        root.get("sizeM2")
                ).as(Float.class);
                addRange(predicates, cb, pricePerM2, req.getPm2From(), req.getPm2To());
            }
            addRange(predicates, cb, root.get("rooms").as(Integer.class), req.getRoomsFrom(), req.getRoomsTo());
            addRange(predicates, cb, root.get("floor").as(Integer.class), req.getFloorsFrom(), req.getFloorsTo());

//            Expression<Integer> floorNumeric = safeParseToInt(cb, root.get("floor"));
//
//            addRange(predicates, cb,
//                    floorNumeric,
//                    req.getFloorsFrom(),
//                    req.getFloorsTo());

            addRange(predicates, cb, root.get("sizeM2"), req.getSizeM2From(), req.getSizeM2To());
            addRange(predicates, cb, root.get("yearBuilt"), req.getYearBuiltFrom(), req.getYearBuiltTo());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Expression<Integer> safeParseToInt(CriteriaBuilder cb, Expression<String> stringField) {
        Expression<String> regexPattern = cb.literal("(-?\\d+)");

        Expression<String> extractedNumber = cb.function(
                "substring",
                String.class,
                stringField,
                regexPattern
        );

        Expression<String> cleanNumber = cb.coalesce(extractedNumber, "0");

        return cleanNumber.as(Integer.class);
    }

    private static <Y extends Comparable<? super Y>> void addRange(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Expression<Y> field,
            Y min,
            Y max) {

        if (min != null && max != null) {
            predicates.add(cb.between(field, min, max));
        } else if (min != null) {
            predicates.add(cb.greaterThanOrEqualTo(field, min));
        } else if (max != null) {
            predicates.add(cb.lessThanOrEqualTo(field, max));
        }
    }
}
