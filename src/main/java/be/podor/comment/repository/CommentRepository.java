package be.podor.comment.repository;

import be.podor.comment.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 리뷰에 달린 댓글들 조회
    @Query(value = "SELECT m, c " +
            "FROM Comment c " +
            "LEFT OUTER JOIN Member m ON m.id = c.createdBy " +
            "WHERE c.review.reviewId = :reviewId " +
            "ORDER BY c.createdAt")
    List<Object[]> findCommentsByReviewId(Long reviewId);

    // 삭제
    void deleteByCommentIdAndCreatedBy(Long commentId, Long createdBy);
}
