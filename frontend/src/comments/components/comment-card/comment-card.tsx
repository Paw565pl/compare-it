"use client";

import { Role } from "@/auth/types/role";
import { hasRequiredRole } from "@/auth/utils/has-required-role";
import { CommentEntity } from "@/comments/entities/comment-entity";
import { useDeleteComment } from "@/comments/hooks/client/use-delete-comment";
import { DeleteConfirmationAlertDialog } from "@/core/components/index";
import { Button } from "@/core/components/ui/button";
import { cn } from "@/core/utils/cn";
import { formatDate } from "@/core/utils/format-date";
import { RatingDto } from "@/rating/dto/rating-dto";
import { useCreateRating } from "@/rating/hooks/client/use-create-rating";
import { useDeleteRating } from "@/rating/hooks/client/use-delete-rating";
import { useUpdateRating } from "@/rating/hooks/client/use-update-rating";
import { Frown, Smile } from "lucide-react";
import { useSession } from "next-auth/react";
import { toast } from "sonner";

interface CommentCardProps {
  comment: CommentEntity;
  productId: string;
}

export const CommentCard = ({ comment, productId }: CommentCardProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const { mutate: deleteComment } = useDeleteComment(
    accessToken,
    productId,
    comment.id,
  );

  const { mutate: createRating, isPending: isCreateRatingPending } =
    useCreateRating(accessToken, productId, comment.id);
  const { mutate: updateRating, isPending: isUpdateRatingPending } =
    useUpdateRating(accessToken, productId, comment.id);
  const { mutate: deleteRating, isPending: isDeleteRatingPending } =
    useDeleteRating(accessToken, productId, comment.id);

  const isRatingButtonDisabled =
    isCreateRatingPending || isUpdateRatingPending || isDeleteRatingPending;

  const isAuthorOrAdmin =
    session?.user?.username === comment.author ||
    hasRequiredRole(session, Role.ADMIN);

  const handleDeleteComment = () => {
    deleteComment(undefined, {
      onSuccess: () => toast.success("Komentarz został usunięty."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const handleMutateRating = (newIsPositive: boolean) => {
    const ratingDto: RatingDto = {
      isPositive: newIsPositive,
    };

    const errorText = "Wystąpił nieoczekiwany błąd! Spróbuj ponownie poźniej.";

    if (comment.isRatingPositive === null)
      createRating(ratingDto, {
        onSuccess: () => toast.success("Twoja ocena została dodana."),
        onError: () => toast.error(errorText),
      });
    else if (comment.isRatingPositive !== newIsPositive)
      updateRating(ratingDto, {
        onSuccess: () => toast.success("Twoja ocena została zaktualizowana."),
        onError: () => toast.error(errorText),
      });
    else if (comment.isRatingPositive === newIsPositive)
      deleteRating(undefined, {
        onSuccess: () => toast.success("Twoja ocena została usunięta."),
        onError: () => toast.error(errorText),
      });
  };

  const formattedCreatedAtDate = formatDate(comment.createdAt);

  return (
    <div className="bg-white p-4">
      <div className="flex items-center gap-4">
        <div className="text-primary text-xl font-semibold">
          {comment.author}
        </div>
        <div className="flex w-full items-center justify-between">
          <div className="text-primary text-center text-sm">
            {formattedCreatedAtDate}
          </div>
          {isAuthorOrAdmin && (
            <DeleteConfirmationAlertDialog
              alertDialogTriggerClassName="bg-white text-red-500 shadow-none hover:bg-background"
              handleDelete={handleDeleteComment}
            />
          )}
        </div>
      </div>

      <div>{comment.text}</div>

      <div className="flex gap-4">
        <div className="flex items-center">
          <Button
            disabled={isRatingButtonDisabled}
            onClick={session ? () => handleMutateRating(true) : undefined}
            className={cn(
              "text-primary bg-white p-1 shadow-none hover:bg-white",
              !session && "cursor-default",
            )}
            title={
              session
                ? "Oceń komentarz pozytywnie"
                : "Zaloguj się, aby móc ocenić komentarz"
            }
          >
            <Smile />
          </Button>
          <span>{comment.positiveRatingsCount}</span>
        </div>

        <div className="flex items-center">
          <Button
            disabled={isRatingButtonDisabled}
            onClick={session ? () => handleMutateRating(false) : undefined}
            className={cn(
              "bg-white p-1 text-gray-500 shadow-none hover:bg-white",
              !session && "cursor-default",
            )}
            title={
              session
                ? "Oceń komentarz negatywnie"
                : "Zaloguj się, aby móc ocenić komentarz"
            }
          >
            <Frown />
          </Button>
          <span>{comment.negativeRatingsCount}</span>
        </div>
      </div>
    </div>
  );
};
