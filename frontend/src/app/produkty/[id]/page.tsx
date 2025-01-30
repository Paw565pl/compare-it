"use client";

import { useParams } from "next/navigation";

const ProductPage = () => {
    const { id } = useParams();

    return <>{id}</>
}

export default ProductPage;