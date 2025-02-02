"use client";

import { CommentForm } from "@/comments/components";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";
import { SingleComment } from "@/products/components/index";

interface ProductPageCommentsProps {
  readonly productId: string;
}

const ProductPageComments = ({ productId }: ProductPageCommentsProps) => {
  const { data: commentsPages } = useFetchCommentPage(productId);

  return (
    <div className="mt-4 flex flex-col gap-2">
      <CommentForm productId={productId} />

      {commentsPages?.pages.map((page) =>
        page.content.map((comment, commentIndex) => (
          <SingleComment
            comment={comment}
            productId={productId}
            key={commentIndex}
          />
        )),
      )}
    </div>
  );
};

export { ProductPageComments };
