import { CommentEntity } from "@/comments/entities/comment-entity";
import { useDeleteComment } from "@/comments/hooks/client/use-delete-comment";
import { Button } from "@/core/components/ui/button";
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

  const isAuthor = session?.user?.username === comment.author;

  const handleDeleteComment = () => {
    deleteComment(undefined, {
      onSuccess: () => toast.success("Komentarz został usunięty."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  return (
    <div className="bg-white p-4">
      {isAuthor && (
        <Button
          onClick={handleDeleteComment}
          className="bg-white text-red-500 shadow-none hover:bg-background"
        >
          <Trash2 />
        </Button>
      )}
      <div>{comment.text}</div>
      <div className="flex gap-1">
        <Button className="bg-white p-1 text-secondary shadow-none hover:bg-white">
          <Smile />
        </Button>
        <Button className="bg-white p-1 text-gray-500 shadow-none hover:bg-white">
          <Frown />
        </Button>
      </div>
    </div>
  );
};
