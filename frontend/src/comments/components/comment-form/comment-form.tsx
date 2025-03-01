"use client";

import { CommentDto } from "@/comments/dtos/comment-dto";
import { useCreateComment } from "@/comments/hooks/client/use-create-comment";
import { Button } from "@/core/components/ui/button";
import { useSession } from "next-auth/react";
import { FormEvent, useRef } from "react";
import { toast } from "sonner";

interface CommentFormProps {
  readonly productId: string;
}

export const CommentForm = ({ productId }: CommentFormProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const inputRef = useRef<HTMLInputElement>(null);
  const { mutate: createComment } = useCreateComment(accessToken, productId);

  const handleCreateComment = (e: FormEvent) => {
    e.preventDefault();
    const text = inputRef.current?.value.trim() || "";
    const commentDto: CommentDto = {
      text,
    };

    createComment(commentDto, {
      onSuccess: () => toast.success("Komentarz został dodany."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  if (session === null)
    return (
      <form className="flex flex-col bg-white">
        <div className="text-secondary m-4 font-semibold">Dodaj Komentarz</div>
        <input
          ref={inputRef}
          min={10}
          disabled
          type="text"
          name="text"
          className="bg-background m-4 mt-0 p-2 text-sm"
          placeholder="Zaloguj się aby dodać komentarz"
        />

        <Button
          disabled
          type="submit"
          className="bg-secondary hover:bg-hover mt-2 w-full font-semibold shadow-none transition-colors duration-200"
        >
          DODAJ KOMENTARZ
        </Button>
      </form>
    );

  return (
    <form onSubmit={handleCreateComment} className="flex flex-col bg-white">
      <div className="text-secondary m-4 font-semibold">Dodaj Komentarz</div>
      <input
        ref={inputRef}
        min={10}
        type="text"
        name="text"
        className="bg-background m-4 mt-0 cursor-pointer p-2 text-sm"
        placeholder="Podziel się swoją opinią..."
      />

      <Button
        type="submit"
        className="bg-secondary hover:bg-hover mt-2 w-full font-semibold shadow-none transition-colors duration-200"
      >
        DODAJ KOMENTARZ
      </Button>
    </form>
  );
};
