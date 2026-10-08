(() => {
	const form = document.getElementById("ai-chat-form");
	if (!form) return;

	const messages = document.getElementById("ai-chat-messages");
	const input = document.getElementById("ai-chat-question");
	const submit = document.getElementById("ai-chat-submit");
	const status = document.getElementById("ai-chat-status");

	function appendMessage(kind, text) {
		const item = document.createElement("article");
		item.className = `ai-chat-message ${kind}`;
		const label = document.createElement("strong");
		label.textContent = kind === "manager" ? "Bạn" : "AI";
		const body = document.createElement("p");
		body.textContent = text;
		item.append(label, body);
		messages.append(item);
		messages.scrollTop = messages.scrollHeight;
		return item;
	}

	form.addEventListener("submit", async (event) => {
		event.preventDefault();
		const question = input.value.trim();
		if (!question || question.length > 1000 || submit.disabled) return;

		const emptyHint = messages.querySelector(".ai-chat-empty");
		if (emptyHint) emptyHint.remove();
		appendMessage("manager", question);
		input.value = "";
		submit.disabled = true;
		status.textContent = "AI đang phân tích câu hỏi…";
		const loading = appendMessage("assistant loading", "Đang xử lý…");

		try {
			const response = await fetch("/manager/ai/chat", {
				method: "POST",
				headers: { "Content-Type": "application/json", Accept: "application/json" },
				body: JSON.stringify({ question })
			});
			const data = await response.json();
			loading.remove();
			if (!response.ok || typeof data.answer !== "string") {
				throw new Error(typeof data.answer === "string" ? data.answer : "Không thể gửi câu hỏi lúc này.");
			}
			appendMessage(data.success ? "assistant" : "assistant error", data.answer);
			status.textContent = "Sẵn sàng nhận câu hỏi tiếp theo.";
		} catch (error) {
			loading.remove();
			appendMessage("assistant error", error.message || "Đã xảy ra lỗi kết nối. Vui lòng thử lại.");
			status.textContent = "Không gửi được câu hỏi.";
		} finally {
			submit.disabled = false;
			input.focus();
		}
	});
})();
