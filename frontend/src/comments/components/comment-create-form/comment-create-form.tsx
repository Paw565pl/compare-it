"use client";

import { useCreateComment } from "@/comments/hooks/client/use-create-comment";
import {
  CommentFormValues,
  commentSchema,
} from "@/comments/schemas/comment-schema";
import { Button } from "@/core/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/core/components/ui/form";
import { Textarea } from "@/core/components/ui/textarea";
import { zodResolver } from "@hookform/resolvers/zod";
import { useSession } from "next-auth/react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

interface CommentFormProps {
  readonly productId: string;
}

export const CommentCreateForm = ({ productId }: CommentFormProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const { mutate: createComment, isPending } = useCreateComment(
    accessToken,
    productId,
  );
  const form = useForm<CommentFormValues>({
    resolver: zodResolver(commentSchema),
    defaultValues: {
      text: "",
    },
  });

  const handleCreateComment = (commentFormValues: CommentFormValues) => {
    createComment(commentFormValues, {
      onSuccess: () => {
        toast.success("Komentarz został dodany.");
        form.reset();
      },
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  return (
    <Form {...form}>
      <form
        onSubmit={session ? form.handleSubmit(handleCreateComment) : undefined}
        className="bg-white"
      >
        <FormField
          name="text"
          control={form.control}
          render={({ field }) => (
            <FormItem className="p-4">
              <FormLabel className="text-primary text-xl font-semibold">
                Dodaj Komentarz
              </FormLabel>
              <FormControl>
                <Textarea
                  disabled={!session}
                  placeholder={
                    session
                      ? "Podziel się swoją opinią..."
                      : "Zaloguj się aby dodać komentarz"
                  }
                  maxWordCount={commentSchema.shape.text.maxLength ?? undefined}
                  className="bg-background border p-2 text-sm"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <Button
          disabled={!session || isPending}
          type="submit"
          className="bg-primary hover:bg-hover mt-2 w-full font-semibold shadow-none transition-colors duration-200"
        >
          DODAJ KOMENTARZ
        </Button>
      </form>
    </Form>
  );
};
