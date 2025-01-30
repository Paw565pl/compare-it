"use client";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";
import { ProductPageProps } from "@/products/pages/product-page";

const ProductPageComments = ({ id }: ProductPageProps) => {
  const { data: commentsData } = useFetchCommentPage(id);

  const commentsMock = ["Komentarz 1", "Komentarz 2", "Komentarz 3"];

  return (
    <div className="mt-4 flex flex-col gap-2">
      {commentsData?.content?.map((comment, commentIndex) => (
        <div>Comment</div>
      ))}
      {commentsMock.map((comment, commentIndex) => (
        <div className="bg-white" key={commentIndex}>
          {comment}
        </div>
      ))}
    </div>
  );
};

export { ProductPageComments };
