"use client";

import { CommentCard, CommentCreateForm } from "@/comments/components";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";
import { useSession } from "next-auth/react";
import InfiniteScroll from "react-infinite-scroll-component";

interface CommentsSectionProps {
  readonly productId: string;
}

export const CommentsSection = ({ productId }: CommentsSectionProps) => {
  const { data: session } = useSession();
  const {
    data: commentPages,
    isFetching: isFetchingComments,
    hasNextPage,
    fetchNextPage,
  } = useFetchCommentPage(productId, session?.tokens?.accessToken);

  const fetchedCommentsCount = commentPages?.pages.reduce(
    (acc, page) => acc + page.content.length,
    0,
  );

  return (
    <section className="mt-4 flex flex-col gap-2">
      <CommentCreateForm productId={productId} />

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
              key={commentIndex}
              comment={comment}
              productId={productId}
              isFetchingComments={isFetchingComments}
            />
          )),
        )}
      </InfiniteScroll>
    </section>
  );
};
