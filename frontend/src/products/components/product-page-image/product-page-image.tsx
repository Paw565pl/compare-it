import Image from "next/image";

interface ProductPageImage {
  readonly name: string;
  readonly imageUrl: string | null;
}

const noImagePlaceholderPath = "/no-image-placeholder.svg";

export const ProductPageImage = ({ name, imageUrl }: ProductPageImage) => {
  return (
    <Image
      src={imageUrl || noImagePlaceholderPath}
      onError={(e) => (e.currentTarget.src = noImagePlaceholderPath)}
      width={400}
      height={400}
      alt={name}
    />
  );
};
