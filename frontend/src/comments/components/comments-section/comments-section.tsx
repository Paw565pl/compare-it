"use client";

import { CommentCard, CommentForm } from "@/comments/components";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";

interface CommentsSectionProps {
  readonly productId: string;
}

export const CommentsSection = ({ productId }: CommentsSectionProps) => {
  const { data: commentPages } = useFetchCommentPage(productId);

  return (
    <section className="mt-4 flex flex-col gap-2">
      <CommentForm productId={productId} />

      {commentPages?.pages.map((page) =>
        page.content.map((comment, commentIndex) => (
          <CommentCard
            comment={comment}
            productId={productId}
            key={commentIndex}
          />
        )),
      )}
    </section>
  );
};
