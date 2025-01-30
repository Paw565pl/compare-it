"use client";
import { useCreateComment } from "@/comments/hooks/client/use-create-comment";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";
import { Button } from "@/core/components/ui/button";
import { ProductPageProps } from "@/products/pages/product-page";

const ProductPageComments = ({ id }: ProductPageProps) => {
  const { data: commentsData } = useFetchCommentPage(id);
  const cdata = useCreateComment();

  const commentsMock = ["Komentarz 1", "Komentarz 2", "Komentarz 3"];

  return (
    <div className="mt-4 flex flex-col gap-2">
      <form className="flex flex-col bg-white">
        <div className="m-4 font-semibold text-secondary">Dodaj Komentarz</div>
        <input
          type="input"
          name="shop"
          className="m-4 mt-0 cursor-pointer bg-background p-2 text-sm"
          placeholder="Podziel się swoją opinią..."
        />
        <Button className="mt-2 w-full bg-secondary font-semibold shadow-none transition-colors duration-200 hover:bg-hover">
          DODAJ KOMENTARZ
        </Button>
      </form>
      {commentsData?.content?.map((comment, commentIndex) => (
        <div>Comment</div>
      ))}
      {commentsMock.map((comment, commentIndex) => (
        <div className="bg-white p-4" key={commentIndex}>
          {comment}
        </div>
      ))}
    </div>
  );
};

export { ProductPageComments };
