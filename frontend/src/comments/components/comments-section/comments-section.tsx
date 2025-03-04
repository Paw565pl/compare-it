"use client";

import { CommentCard, CommentForm } from "@/comments/components";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";
import InfiniteScroll from "react-infinite-scroll-component";

interface CommentsSectionProps {
  readonly productId: string;
}

export const CommentsSection = ({ productId }: CommentsSectionProps) => {
  const {
    data: commentPages,
    hasNextPage,
    fetchNextPage,
  } = useFetchCommentPage(productId);

  const fetchedCommentsCount = commentPages?.pages.reduce(
    (acc, page) => acc + page.content.length,
    0,
  );

  return (
    <section className="mt-4 flex flex-col gap-2">
      <CommentForm productId={productId} />

      <InfiniteScroll
        dataLength={fetchedCommentsCount || 0}
        hasMore={hasNextPage}
        next={fetchNextPage}
        loader={null}
        className="space-y-3"
      >
        {commentPages?.pages.map((page) =>
          page.content.map((comment, commentIndex) => (
            <CommentCard
              comment={comment}
              productId={productId}
              key={commentIndex}
            />
          )),
        )}
      </InfiniteScroll>
    </section>
  );
};
