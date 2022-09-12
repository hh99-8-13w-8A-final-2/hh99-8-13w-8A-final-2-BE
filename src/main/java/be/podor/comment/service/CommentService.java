package be.podor.comment.service;

import be.podor.comment.dto.CommentRequestDto;
import be.podor.comment.dto.CommentResponseDto;
import be.podor.comment.model.Comment;
import be.podor.comment.repository.CommentRepository;
import be.podor.member.model.Member;
import be.podor.member.repository.MemberRepository;
import be.podor.review.model.Review;
import be.podor.review.repository.ReviewRepository;
import be.podor.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final ReviewRepository reviewRepository;

    private final MemberRepository memberRepository;

    // 리뷰에 달린 댓글들 조회
    public List<CommentResponseDto> findReviewComments(Long reviewId) {
        List<Object[]> queryResult = commentRepository.findCommentsByReviewId(reviewId);

        return queryResult.stream()
                .map(obj -> CommentResponseDto.of((Member) obj[0], (Comment) obj[1]))
                .collect(Collectors.toList());
    }

    // 리뷰 댓글 작성
    public CommentResponseDto createReviewComment(Long reviewId, CommentRequestDto requestDto, UserDetailsImpl userDetails) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(
                        () -> new IllegalArgumentException(reviewId + "번에 해당하는 리뷰가 존재하지 않습니다.")
                );

        Member member = memberRepository.findById(userDetails.getMemberId()).orElseThrow();
        Comment comment = commentRepository.save(Comment.of(requestDto, review));

        return CommentResponseDto.of(member, comment);
    }

    // 리뷰 댓글 수정
    @Transactional
    public CommentResponseDto updateReviewComment(Long commentId, CommentRequestDto requestDto, UserDetailsImpl userDetails) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException(commentId + "번에 해당하는 댓글이 존재하지 않습니다.")
        );

        comment.update(requestDto);
        Member member = memberRepository.findById(userDetails.getMemberId()).orElseThrow();

        return CommentResponseDto.of(member, comment);
    }

    // 리뷰 댓글 삭제
    @Transactional
    public void deleteReviewComment(Long commentId, UserDetailsImpl userDetails) {
        commentRepository.deleteByCommentIdAndCreatedBy(commentId, userDetails.getMemberId());
    }
}