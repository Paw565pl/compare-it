"use client";

import { Role } from "@/auth/types/role";
import { hasRequiredRole } from "@/auth/utils/has-required-role";
import { CommentUpdateForm } from "@/comments/components/comment-update-form/comment-update-form";
import { CommentEntity } from "@/comments/entities/comment-entity";
import { useDeleteComment } from "@/comments/hooks/client/use-delete-comment";
import { DeleteConfirmationAlertDialog } from "@/core/components";
import { Button } from "@/core/components/ui/button";
import { formatDate } from "@/core/utils/format-date";
import { RatingDto } from "@/rating/dto/rating-dto";
import { useCreateRating } from "@/rating/hooks/client/use-create-rating";
import { useDeleteRating } from "@/rating/hooks/client/use-delete-rating";
import { useUpdateRating } from "@/rating/hooks/client/use-update-rating";
import { Frown, Pencil, Smile, Trash2 } from "lucide-react";
import { useSession } from "next-auth/react";
import { useState } from "react";
import { toast } from "sonner";

interface CommentCardProps {
  readonly comment: CommentEntity;
  readonly productId: string;
}

export const CommentCard = ({ comment, productId }: CommentCardProps) => {
  const [isEditModeEnabled, setIsEditModeEnabled] = useState(false);

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

  const isAuthor = session?.user?.username === comment.author;
  const isAuthorOrAdmin = isAuthor || hasRequiredRole(session, Role.ADMIN);

  const handleDeleteComment = () => {
    deleteComment(undefined, {
      onSuccess: () => toast.success("Komentarz został usunięty."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const handleMutateRating = (newIsPositive: boolean) => {
    if (!session)
      return toast.info("Musisz być zalogowany, aby ocenić komentarz.");

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
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 sm:gap-4">
          <div className="text-primary text-xl font-semibold">
            {comment.author}
          </div>
          <div className="text-primary text-center text-sm">
            {formattedCreatedAtDate}
          </div>
        </div>

        {!isEditModeEnabled && (
          <div className="flex items-center gap-1">
            {isAuthor && (
              <Button
                variant="commentAction"
                className="text-gray-500"
                aria-label="Edytuj komentarz"
                onClick={() => setIsEditModeEnabled(true)}
              >
                <Pencil />
              </Button>
            )}
            {isAuthorOrAdmin && (
              <DeleteConfirmationAlertDialog
                trigger={
                  <Button
                    variant="commentAction"
                    className="text-red-500"
                    aria-label="Usuń komentarz"
                  >
                    <Trash2 />
                  </Button>
                }
                handleDelete={handleDeleteComment}
              />
            )}
          </div>
        )}
      </div>

      {isEditModeEnabled ? (
        <CommentUpdateForm
          comment={comment}
          productId={productId}
          setIsEditModeEnabled={setIsEditModeEnabled}
        />
      ) : (
        <p>{comment.text}</p>
      )}

      {!isEditModeEnabled && (
        <div className="mt-1 flex gap-4">
          <div className="flex items-center gap-1">
            <Button
              size="noPadding"
              disabled={isRatingButtonDisabled}
              onClick={() => handleMutateRating(true)}
              aria-label="Oceń komentarz pozytywnie"
              className="text-primary hover:text-primary/70 h-fit bg-white shadow-none hover:bg-white"
            >
              <Smile />
            </Button>
            <span>{comment.positiveRatingsCount}</span>
          </div>

          <div className="flex items-center gap-1">
            <Button
              size="noPadding"
              disabled={isRatingButtonDisabled}
              onClick={() => handleMutateRating(false)}
              aria-label="Oceń komentarz negatywnie"
              className="h-fit bg-white text-gray-500 shadow-none hover:bg-white hover:text-gray-500/70"
            >
              <Frown />
            </Button>
            <span>{comment.negativeRatingsCount}</span>
          </div>
        </div>
      )}
    </div>
  );
};
