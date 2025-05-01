"use client";

import { CommentEntity } from "@/comments/entities/comment-entity";
import { useUpdateComment } from "@/comments/hooks/client/use-update-comment";
import {
  CommentFormValues,
  commentSchema,
} from "@/comments/schemas/comment-schema";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/core/components/ui/alert-dialog";
import { Button } from "@/core/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormMessage,
} from "@/core/components/ui/form";
import { Textarea } from "@/core/components/ui/textarea";
import { zodResolver } from "@hookform/resolvers/zod";
import { Ban, MoveLeft } from "lucide-react";
import { useSession } from "next-auth/react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

interface CommentUpdateFormProps {
  readonly comment: CommentEntity;
  readonly productId: string;
  readonly setIsEditModeEnabled: (isEditModeEnabled: boolean) => void;
}

export const CommentUpdateForm = ({
  comment,
  productId,
  setIsEditModeEnabled,
}: CommentUpdateFormProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const form = useForm<CommentFormValues>({
    resolver: zodResolver(commentSchema),
    defaultValues: {
      text: comment.text,
    },
  });

  const { mutate: updateComment, isPending } = useUpdateComment(
    accessToken,
    productId,
    comment.id,
  );

  const handleUpdateComment = (commentFormValues: CommentFormValues) => {
    updateComment(commentFormValues, {
      onSuccess: () => {
        toast.success("Komentarz został zaktualizowany.");
        form.reset();
        setIsEditModeEnabled(false);
      },
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const handleCancelEdit = () => {
    form.reset();
    setIsEditModeEnabled(false);
  };

  return (
    <Form {...form}>
      <form
        className="space-y-4 pt-2"
        onSubmit={form.handleSubmit(handleUpdateComment)}
      >
        <FormField
          name="text"
          control={form.control}
          render={({ field }) => (
            <FormItem>
              <FormControl>
                <Textarea
                  initialWordCount={comment.text.length}
                  maxWordCount={commentSchema.shape.text.maxLength ?? undefined}
                  className="bg-background border p-2 text-sm"
                  {...field}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <div className="flex items-center gap-4">
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button
                type="button"
                variant="outline"
                className="ml-auto"
                disabled={isPending}
              >
                ANULUJ ZMIANY
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Czy jesteś pewny?</AlertDialogTitle>
                <AlertDialogDescription>
                  Utracisz wszystkie wprowadzone zmiany.
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel>
                  <MoveLeft /> WRÓĆ
                </AlertDialogCancel>
                <AlertDialogAction
                  variant="destructive"
                  onClick={handleCancelEdit}
                >
                  <Ban /> ANULUJ
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>

          <Button type="submit" disabled={isPending}>
            ZAPISZ
          </Button>
        </div>
      </form>
    </Form>
  );
};
