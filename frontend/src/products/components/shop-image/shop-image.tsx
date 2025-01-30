import Image from "next/image";

interface ShopImageProps {
  readonly name: string;
  readonly imageUrl: string | null;
}

const noImagePlaceholderPath = "/no-image-placeholder.svg";

export const ShopImage = ({ name, imageUrl }: ShopImageProps) => {
  return (
    <Image
      src={imageUrl || noImagePlaceholderPath}
      onError={(e) => (e.currentTarget.src = noImagePlaceholderPath)}
      width={100}
      height={100}
      alt={name}
    />
  );
};
