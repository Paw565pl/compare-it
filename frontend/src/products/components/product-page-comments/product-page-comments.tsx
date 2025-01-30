"use client";
import { useFetchCommentPage } from "@/comments/hooks/client/use-fetch-comment-page";
import { Button } from "@/core/components/ui/button";
import { ProductPageProps } from "@/products/pages/product-page";
import { Frown, Smile } from "lucide-react";

const ProductPageComments = ({ id }: ProductPageProps) => {
  const { data: commentsData } = useFetchCommentPage(id);

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
          <div>{comment}</div>
          <div className="flex gap-1">
            <Button className="bg-white p-1 text-secondary shadow-none hover:bg-white">
              <Smile />
            </Button>
            <Button className="bg-white p-1 text-gray-500 shadow-none hover:bg-white">
              <Frown />
            </Button>
          </div>
        </div>
      ))}
    </div>
  );
};

export { ProductPageComments };
