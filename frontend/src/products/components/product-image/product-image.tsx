import Image from "next/image";

interface ProductImageProps {
  readonly name: string;
  readonly imageUrl: string | null;
}

const noImagePlaceholderPath = "/no-image-placeholder.svg";

export const ProductImage = ({ name, imageUrl }: ProductImageProps) => {
  return (
    <Image
      src={imageUrl || noImagePlaceholderPath}
      onError={(e) => (e.currentTarget.src = noImagePlaceholderPath)}
      width={200}
      height={200}
      alt={name}
      className="rounded-md"
    />
  );
};
