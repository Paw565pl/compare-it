import { CommentEntity } from "@/comments/entities/comment-entity";
import { useDeleteComment } from "@/comments/hooks/client/use-delete-comment";
import { Button } from "@/core/components/ui/button";
import { RatingDto } from "@/rating/dto/rating-dto";
import { useCreateRating } from "@/rating/hooks/client/use-create-rating";
import { Frown, Smile, Trash2 } from "lucide-react";
import { useSession } from "next-auth/react";
import { toast } from "sonner";

interface SingleCommentProps {
  comment: CommentEntity;
  productId: string;
}

export const SingleComment = ({ comment, productId }: SingleCommentProps) => {
  const { data: session } = useSession();

  const accessToken = session?.tokens?.accessToken as string;

  const { mutate: deleteComment } = useDeleteComment(
    accessToken,
    productId,
    comment.id,
  );

  const { mutate: createRating } = useCreateRating(
    accessToken,
    productId,
    comment.id,
  );

  const isAuthor = session?.user?.username === comment.author;

  const handleDeleteComment = () => {
    deleteComment(undefined, {
      onSuccess: () => toast.success("Komentarz został usunięty."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const handleCreatePositiveRating = () => {
    const ratingDto: RatingDto = {
      isPositive: true,
    };
    createRating(ratingDto, {
      onSuccess: () => toast.success("Pozytywnie oceniono komentarz."),
      onError: () => toast.error("Już oceniłeś ten komentarz!"),
    });
  };

  const handleCreateNegativeRating = () => {
    const ratingDto: RatingDto = {
      isPositive: false,
    };
    createRating(ratingDto, {
      onSuccess: () => toast.success("Negatywnie oceniono komentarz."),
      onError: () => toast.error("Już oceniłeś ten komentarz!"),
    });
  };

  const createdAtDate = new Date(comment.createdAt);
  const formattedCreatedAtDate = new Intl.DateTimeFormat("pl-PL", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(createdAtDate);

  return (
    <div className="bg-white p-4">
      <div className="flex items-center gap-4">
        <div className="text-xl font-semibold text-secondary">
          {comment.author}
        </div>
        <div className="text-sm text-muted">{formattedCreatedAtDate}</div>
        {isAuthor && (
          <Button
            onClick={handleDeleteComment}
            className="bg-white p-1 text-red-500 shadow-none hover:bg-background"
          >
            <Trash2 />
          </Button>
        )}
      </div>
      <div>{comment.text}</div>
      <div className="flex gap-4">
        <div className="flex items-center">
          <Button
            onClick={handleCreatePositiveRating}
            className="bg-white p-1 text-secondary shadow-none hover:bg-white"
          >
            <Smile />
          </Button>
          <div>{comment.positiveRatingsCount}</div>
        </div>
        <div className="flex items-center">
          <Button
            onClick={handleCreateNegativeRating}
            className="bg-white p-1 text-gray-500 shadow-none hover:bg-white"
          >
            <Frown />
          </Button>
          <div>{comment.negativeRatingsCount}</div>
        </div>
      </div>
    </div>
  );
};
